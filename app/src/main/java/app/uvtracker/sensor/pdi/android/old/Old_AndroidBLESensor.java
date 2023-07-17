package app.uvtracker.sensor.pdi.android.old;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import app.uvtracker.sensor.api.Old_IPacketDrivenSensor;
import app.uvtracker.sensor.protocol.codec.IPacketCodec;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.packet.Packet;

public class Old_AndroidBLESensor implements Old_IPacketDrivenSensor {

    @NonNull
    private static final String TAG = Old_AndroidBLESensor.class.getSimpleName();

    @NonNull
    private final BluetoothDevice device;

    @Nullable
    private final String name;

    @NonNull
    private final Context context;

    @NonNull
    private final Old_EventRegistry<Consumer<ConnectionStatus>> connectionCallbackRegistry;

    @NonNull
    private final Old_EventRegistry<Consumer<Packet>> packetCallbackRegistry;

    @NonNull
    private final Old_AndroidBLESensorConnection connection;

    @SuppressLint("MissingPermission")
    public Old_AndroidBLESensor(@NonNull BluetoothDevice device, @NonNull Context context) {
        this.device = device;
        this.name = device.getName();
        this.context = context;
        this.connectionCallbackRegistry = new Old_EventRegistry<>();
        this.packetCallbackRegistry = new Old_EventRegistry<>();
        this.connection = new Old_AndroidBLESensorConnection(
                this,
                (s) -> this.connectionCallbackRegistry.invoke((f) -> f.accept(s)),
                (s) -> {
                    Packet packet = this.parsePacket(s);
                    if(packet == null) return;
                    this.packetCallbackRegistry.invoke((f) -> f.accept(packet));
                }
        );
    }

    @NonNull
    @Override
    public String getAddress() {
        return this.device.getAddress();
    }

    @Nullable
    @Override
    public String getName() {
        return this.name;
    }

    @NonNull
    public BluetoothDevice getPlatformDevice() {
        return this.device;
    }

    @NonNull
    public Context getPlatformContext() {
        return this.context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Old_AndroidBLESensor)) return false;
        Old_AndroidBLESensor that = (Old_AndroidBLESensor) o;
        return this.device.getAddress().equals(that.device.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.device.getAddress());
    }

    // Connection flow:
    @Override
    public boolean isConnected() {
        return this.connection.isConnected();
    }

    @Override
    public boolean connect() {
        return this.connection.connect();
    }

    @Override
    public boolean disconnect() {
        return this.connection.disconnect();
    }

    @Override
    public void forceReset() {
        this.connection.reset();
    }

    // Connection flow events:
    @Override
    public boolean registerConnectionStatusCallback(@NonNull Consumer<ConnectionStatus> callback) {
        return this.connectionCallbackRegistry.register(callback);
    }

    @Override
    public boolean unregisterConnectionStatusCallback(@NonNull Consumer<ConnectionStatus> callback) {
        return this.connectionCallbackRegistry.unregister(callback);
    }

    // Packet infrastructure:
    @Override
    public boolean registerPacketReceptionCallback(@NonNull Consumer<Packet> callback) {
        return this.packetCallbackRegistry.register(callback);
    }

    @Override
    public boolean unregisterPacketReceptionCallback(@NonNull Consumer<Packet> callback) {
        return this.packetCallbackRegistry.unregister(callback);
    }

    @Override
    public boolean sendPacket(@NonNull Packet packet) {
        // TODO: packet fragmentation
        // NOTE: if fragmentation is implemented, remove MTU specification in codec impl.
        if(!this.connection.isConnected()) return false;
        try {
            String encoded = IPacketCodec.get().encode(packet);
            return this.connection.write("#" + encoded + "\r\n");
        }
        catch(CodecException e) {
            Log.d(TAG, "Packet sending: unable to send packet. Caused by: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    private Packet parsePacket(byte[] input) {
        String inputString = new String(input, StandardCharsets.US_ASCII).trim();
        Log.d(TAG, "Packet parsing: processing \"" + inputString + "\"");
        if(!inputString.startsWith("#")) {
            Log.d(TAG, "Packet parsing: input does not start with a '#'.");
            return null;
        }
        try {
            Packet packet = IPacketCodec.get().decode(inputString.substring(1));
            if(packet.isBaseType()) Log.d(TAG, "Obtained base type. " + packet);
            else Log.d(TAG, "Obtained specific type! " + packet);
            return packet;
        } catch (CodecException e) {
            Log.d(TAG, "Packet parsing: unable to parse packet. Caused by: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    // Features: WIP


}


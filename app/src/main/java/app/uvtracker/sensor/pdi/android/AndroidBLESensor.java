package app.uvtracker.sensor.pdi.android;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import app.uvtracker.sensor.pdi.android.connection.bytestream.AndroidBLESensorBytestreamConnection;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.ISensorConnection;
import app.uvtracker.sensor.pii.connection.application.SensorConnection;
import app.uvtracker.sensor.pii.connection.bytestream.ISensorBytestreamConnection;
import app.uvtracker.sensor.pii.connection.packet.ISensorPacketConnection;
import app.uvtracker.sensor.pii.connection.packet.SensorPacketConnection;

public class AndroidBLESensor implements ISensor {

    public static class ConnectionFactory {

        @NonNull
        public final ISensorBytestreamConnection bytestreamBased;

        @NonNull
        public final ISensorPacketConnection packetBased;

        @NonNull
        public final ISensorConnection applicationHandle;

        public ConnectionFactory(@NonNull BluetoothDevice device, @NonNull Context context) {
            this.bytestreamBased = new AndroidBLESensorBytestreamConnection(device, context);
            this.packetBased = new SensorPacketConnection(this.bytestreamBased);
            this.applicationHandle = new SensorConnection(this.packetBased);
        }

    }

    @NonNull
    private final Context context;

    @NonNull
    private final BluetoothDevice platformDevice;

    @NonNull
    private final String address;

    @NonNull
    private final String name;

    private int rssi;

    @NonNull
    private Date lastSeenAt;

    @Nullable
    private ConnectionFactory factory;

    @SuppressLint("MissingPermission")
    public AndroidBLESensor(@NonNull ScanResult result, @NonNull Context context) {
        this.context = context;
        this.platformDevice = result.getDevice();
        this.address = this.platformDevice.getAddress();
        this.name = this.platformDevice.getName();
        this.rssi = result.getRssi();
        this.lastSeenAt = new Date();
    }

    @NonNull
    @Override
    public String getAddress() {
        return this.address;
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getRssi() {
        return this.rssi;
    }

    @NonNull
    @Override
    public Date getLastSeenAt() {
        return this.lastSeenAt;
    }

    @NonNull
    @Override
    public ISensorConnection getConnection() {
        return this.getFactoryBuilds().applicationHandle;
    }

    public void update(@NonNull ScanResult result) {
        this.rssi = result.getRssi();
        this.lastSeenAt = new Date();
    }

    @NonNull
    public BluetoothDevice getPlatformDevice() {
        return this.platformDevice;
    }

    @NonNull
    public ConnectionFactory getFactoryBuilds() {
        if(this.factory == null)
            this.factory = new ConnectionFactory(this.platformDevice, this.context);
        return this.factory;
    }

}


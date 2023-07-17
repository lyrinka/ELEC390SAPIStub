package app.uvtracker.sensor.api;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import app.uvtracker.sensor.protocol.packet.Packet;

public interface Old_IPacketDrivenSensor extends Old_ISensor {

    boolean sendPacket(@NonNull Packet packet);

    boolean registerPacketReceptionCallback(@NonNull Consumer<Packet> callback);

    boolean unregisterPacketReceptionCallback(@NonNull Consumer<Packet> callback);

}

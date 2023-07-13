package app.uvtracker.sensor.api;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import app.uvtracker.sensor.api.ISensor;
import app.uvtracker.sensor.protocol.packet.Packet;

public interface IPacketDrivenSensor extends ISensor {

    boolean sendPacket(@NonNull Packet packet);

    boolean registerPacketReceptionCallback(@NonNull Consumer<Packet> callback);

    boolean unregisterPacketReceptionCallback(@NonNull Consumer<Packet> callback);

}

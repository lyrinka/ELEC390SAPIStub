package app.uvtracker.sensor.api;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import app.uvtracker.sensor.api.exception.comms.CommunicationException;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.packet.Packet;

public interface IPacketDrivenSensor extends ISensor {

    void sendPacket(@NonNull Packet packet) throws CodecException, CommunicationException;

    boolean registerPacketReceptionCallback(@NonNull Consumer<Packet> callback);

    boolean unregisterPacketReceptionCallback(@NonNull Consumer<Packet> callback);

}

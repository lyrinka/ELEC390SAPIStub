package app.uvtracker.sensor.pii.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pii.connection.IConnectable;
import app.uvtracker.sensor.protocol.packet.Packet;

public interface ISensorPacketConnection extends IConnectable {

    boolean write(@NonNull Packet packet);

}

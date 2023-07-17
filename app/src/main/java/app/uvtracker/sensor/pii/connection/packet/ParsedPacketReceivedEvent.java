package app.uvtracker.sensor.pii.connection.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.Packet;

public class ParsedPacketReceivedEvent extends PacketReceivedEvent {

    public ParsedPacketReceivedEvent(@NonNull Packet packet) {
        super(packet);
    }

}

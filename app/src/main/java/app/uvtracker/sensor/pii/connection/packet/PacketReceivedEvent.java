package app.uvtracker.sensor.pii.connection.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.Packet;

public class PacketReceivedEvent {

    @NonNull
    public static PacketReceivedEvent fromPacket(@NonNull Packet packet) {
        if(packet.isBaseType()) return new PacketReceivedEvent(packet);
        else return new ParsedPacketReceivedEvent(packet);
    }

    @NonNull
    private final Packet packet;

    public PacketReceivedEvent(@NonNull Packet packet) {
        this.packet = packet;
    }

    @NonNull
    public Packet getPacket() {
        return packet;
    }

}

package app.uvtracker.sensor.protocol.packet;

import androidx.annotation.NonNull;

import java.util.Arrays;

import app.uvtracker.sensor.protocol.type.PacketType;

public class Packet {

    @NonNull
    private final PacketType type;

    @NonNull
    private byte[] payload;

    public Packet(@NonNull PacketType type, @NonNull byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public Packet(@NonNull PacketType type) {
        this(type, new byte[0]);
    }

    public Packet(@NonNull Packet obj) {
        // Note: this constructor does not copy internal payload array
        this(obj.type, obj.payload);
    }

    @NonNull
    public PacketType getType() {
        return type;
    }

    @NonNull
    public byte[] getPayload() {
        // Note: this method does not copy internal payload array
        return payload;
    }

    public void setPayload(@NonNull byte[] payload) {
        this.payload = payload;
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder(this.payload.length * 3 + 1);
        sb.append("[");
        for(byte data : this.payload) sb.append(String.format("%02x ", data));
        if(sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return this.type + sb.toString();
    }

}

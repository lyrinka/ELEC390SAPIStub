package com.example.elec390.sapi.protocol;

import com.example.elec390.sapi.protocol.type.PacketType;

import java.util.Arrays;

public class PacketBase {

    private final PacketType packetType;
    private byte[] payload;

    public PacketBase(PacketBase obj) {
        this(obj.packetType, obj.payload);
    }

    public PacketBase(PacketType packetType) {
        this(packetType, new byte[0]);
    }

    public PacketBase(PacketType packetType, byte[] payload) {
        this.packetType = packetType;
        this.payload = payload;
    }

    public PacketType getPacketType() {
        return this.packetType;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    @Override
    public String toString() {
        return "PacketBase{" +
                "type=" + packetType +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }

}

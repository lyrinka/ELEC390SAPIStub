package com.example.elec390.sapi.protocol.type;

import java.util.Objects;

public final class PacketType {

    public static class Data {

        public enum In {
            SAMPLE, MULTI_SAMPLE,
        }
        public enum Out {
            REQUEST, REQUEST_MULTI,
        }

    }

    private final PacketDirection packetDirection;
    private final byte packetID;

    public PacketType(PacketDirection packetDirection, byte packetID) {
        this.packetDirection = packetDirection;
        this.packetID = packetID;
    }

    public PacketDirection getPacketDirection() {
        return packetDirection;
    }

    public byte getPacketID() {
        return packetID;
    }

    @Override
    public String toString() {
        return "PacketType{" +
                "dir=" + packetDirection +
                ", pid=" + packetID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacketType)) return false;
        PacketType that = (PacketType) o;
        return packetID == that.packetID && packetDirection == that.packetDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetDirection, packetID);
    }

}

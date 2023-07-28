package app.uvtracker.sensor.protocol.packet.type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalEstimation;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalSample;

/*
    Packet ID range conventions:
        0x00 to 0x1F: System packets
        0x20 to 0x3F: Data transfer & request packets
        0x40 to 0x5F: HMI packets
        0x60 to 0x6F: Reserved
        0x70 to 0x7F: Debug & tracing
 */
public interface PacketType {

    enum IN implements PacketType {

        // System: 0x00 ~ 0x1F
        // HMI: 0x10 ~ 0x2F
        // Data: 0x20 ~ 0x3F
        NEW_OPTICAL_SAMPLE          (0x20, PacketInNewOpticalSample.class),
        NEW_OPTICAL_ESTIMATION      (0x21, PacketInNewOpticalEstimation.class),

        ;

        private final int packetID;

        @NonNull
        private final Class<? extends Packet> clazz;

        IN(int packetID, @NonNull Class<? extends Packet> clazz) {
            this.packetID = packetID;
            this.clazz = clazz;
        }

        @Override
        public int getID() {
            return this.packetID;
        }

        @Override
        @NonNull
        public PacketDirection getDirection() {
            return PacketDirection.IN;
        }

        @Override
        @NonNull
        public Class<? extends Packet> getPacketClass() {
            return this.clazz;
        }

        @Override
        @NonNull
        public String toString() {
            return "PacketIn" + Arrays.stream(super.toString().split("_")).map((s) -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining());
        }

        private static final HashMap<Integer, IN> map = new HashMap<>();

        static {
            for(IN e : IN.values()) map.put(e.packetID, e);
        }

        @Nullable
        public static PacketType getByID(int packetID) {
            return IN.map.get(packetID);
        }

    }

    enum OUT implements PacketType {

        // System: 0x00 ~ 0x1F
        // HMI: 0x10 ~ 0x2F
        // Data: 0x20 ~ 0x3F

        ;

        private final int packetID;

        @NonNull
        private final Class<? extends Packet> clazz;

        OUT(int packetID, @NonNull Class<? extends Packet> clazz) {
            this.packetID = packetID;
            this.clazz = clazz;
        }

        @Override
        public int getID() {
            return this.packetID;
        }

        @Override
        @NonNull
        public PacketDirection getDirection() {
            return PacketDirection.OUT;
        }

        @Override
        @NonNull
        public Class<? extends Packet> getPacketClass() {
            return this.clazz;
        }

        @Override
        @NonNull
        public String toString() {
            return "PacketOut" + Arrays.stream(super.toString().split("_")).map((s) -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining());
        }

        private static final HashMap<Integer, OUT> map = new HashMap<>();

        static {
            for(OUT e : OUT.values()) map.put(e.packetID, e);
        }

        @Nullable
        public static PacketType getByID(int packetID) {
            return OUT.map.get(packetID);
        }

    }


    int getID();

    @NonNull
    PacketDirection getDirection();

    @NonNull
    Class<? extends Packet> getPacketClass();

    @Nullable
    default Constructor<? extends Packet> getPacketConstructor() {
        try {
            return this.getPacketClass().getConstructor(Packet.class);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    @Override
    @NonNull
    String toString();

}

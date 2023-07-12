package app.uvtracker.sensor.protocol.type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public interface PacketType {

    enum IN implements PacketType {

        KEEP_ALIVE(0),
        DATA_SINGLE(1);

        final int packetID;

        IN(int packetID) {
            this.packetID = packetID;
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

        KEEP_ALIVE(0),
        BUZZ(1);

        final int packetID;

        OUT(int packetID) {
            this.packetID = packetID;
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

    @NonNull PacketDirection getDirection();

    @Override
    @NonNull String toString();

}

package app.uvtracker.sensor.protocol.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInNewSample extends PacketIn {

    private final int intensityUV;

    private final int intensityVIS;

    public PacketInNewSample(Packet packetBase) throws PacketFormatException {
        super(packetBase);
        PacketFormatException.requireLength(packetBase, 6);
        this.intensityVIS = Packing.unpack3(this.payload, 0);
        this.intensityUV  = Packing.unpack3(this.payload, 3);
    }

    public int getIntensityUV() {
        return intensityUV;
    }

    public int getIntensityVIS() {
        return intensityVIS;
    }

    @Override
    @NonNull
    public String toString() {
        return this.type + String.format("{VIS=%1$d,UV=%2$d}", this.intensityVIS, this.intensityUV);
    }

}

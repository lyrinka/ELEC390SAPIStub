package app.uvtracker.sensor.protocol.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;

public class PacketInNewSample extends PacketIn {

    private final int intensityUV;

    private final int intensityVIS;

    public PacketInNewSample(Packet packetBase) throws PacketFormatException {
        super(packetBase);
        if(this.payload.length != 6)
            throw new PacketFormatException("Expected 6 bytes.", packetBase);
        this.intensityVIS = (this.payload[0] << 16) + (this.payload[1] << 8) + this.payload[2];
        this.intensityUV  = (this.payload[3] << 16) + (this.payload[4] << 8) + this.payload[5];
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

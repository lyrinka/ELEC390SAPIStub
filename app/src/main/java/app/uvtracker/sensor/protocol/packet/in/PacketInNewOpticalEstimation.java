package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.data.type.OpticalRecord;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInNewOpticalEstimation extends PacketIn {

    private final int sampleNumber;

    private final int sampleInterval;

    @NonNull
    private final OpticalRecord record;

    public PacketInNewOpticalEstimation(@NonNull Packet packetBase) throws PacketFormatException {
        super(packetBase);
        packetBase.requireLength(7);
        this.sampleNumber = Packing.unpack4(packetBase.getPayload(), 0);
        this.sampleInterval =  Packing.unpack1(packetBase.getPayload(), 6);
        this.record = Packing.unpackOpticalRecord(packetBase.getPayload(), 4);
    }

    public int getSampleNumber() {
        return this.sampleNumber;
    }

    public int getSampleInterval() {
        return this.sampleInterval;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}

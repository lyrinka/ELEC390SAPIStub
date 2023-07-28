package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.data.type.OpticalRecord;
import app.uvtracker.data.type.RemoteTimestamp;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInNewOpticalSample extends PacketIn {

    @NonNull
    private final RemoteTimestamp timestamp;

    @NonNull
    private final OpticalRecord record;

    public PacketInNewOpticalSample(@NonNull Packet packetBase) throws PacketFormatException {
        super(packetBase);
        packetBase.requireLength(6);
        this.timestamp = Packing.unpackRemoteTimestamp(packetBase.getPayload(), 0, 1);
        this.record = Packing.unpackOpticalRecord(packetBase.getPayload(), 2);
    }

    @NonNull
    public RemoteTimestamp getTimestamp() {
        return timestamp;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}

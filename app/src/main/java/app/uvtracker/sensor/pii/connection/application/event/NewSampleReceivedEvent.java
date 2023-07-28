package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import app.uvtracker.data.type.OpticalRecord;
import app.uvtracker.data.type.RemoteTimestamp;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalSample;

public class NewSampleReceivedEvent {

    @NonNull
    private final RemoteTimestamp remoteTimestamp;

    @NonNull
    private final OpticalRecord record;

    public NewSampleReceivedEvent(@NonNull PacketInNewOpticalSample packet) {
        this.remoteTimestamp = packet.getTimestamp();
        this.record = packet.getRecord();
    }

    @NonNull
    public RemoteTimestamp getRemoteTimestamp() {
        return remoteTimestamp;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}

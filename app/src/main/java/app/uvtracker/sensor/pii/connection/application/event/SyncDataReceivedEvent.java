package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.SampleTimestamp;

public class SyncDataReceivedEvent {

    @NonNull
    private final SampleTimestamp time;

    @NonNull
    private final OpticalRecord record;

    public SyncDataReceivedEvent(@NonNull SampleTimestamp time, @NonNull OpticalRecord record) {
        this.time = time;
        this.record = record;
    }

    @NonNull
    public SampleTimestamp getTime() {
        return time;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}

package app.uvtracker.sensor.pii.connection;

import androidx.annotation.NonNull;

public class DataReceivedEvent {

    @NonNull
    private final byte[] data;

    public DataReceivedEvent(@NonNull byte[] data) {
        this.data = data;
    }

    @NonNull
    public byte[] getData() {
        return data;
    }

}

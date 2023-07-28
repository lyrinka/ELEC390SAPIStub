package app.uvtracker.data.type;

import androidx.annotation.NonNull;

import java.util.Date;

public class RemoteTimestamp {

    public final int sampleInterval;

    public final long sampleNumber;

    @NonNull
    private final Date asLocalTimestamp;

    public RemoteTimestamp(int sampleInterval, long sampleNumber) {
        this.sampleInterval = sampleInterval;
        this.sampleNumber = sampleNumber;
        this.asLocalTimestamp = new Date(System.currentTimeMillis() + sampleInterval * sampleNumber);
    }

    @NonNull
    public Date toLocalTime() {
        return this.asLocalTimestamp;
    }

}

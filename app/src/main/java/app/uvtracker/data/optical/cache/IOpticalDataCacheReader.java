package app.uvtracker.data.optical.cache;

import androidx.annotation.Nullable;

import java.util.Date;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.pii.event.IEventSource;

public interface IOpticalDataCacheReader extends IEventSource {

    // Emits event: OpticalDataCacheUpdateEvent

    long getDeviceBootTime();

    long getSampleInterval();

    int getStartIndex();

    int getEndIndex();

    default int getSize() {
        if(this.getStartIndex() < 0 || this.getEndIndex() < 0) return 0;
        return this.getStartIndex() - this.getEndIndex() + 1;
    }

    @Nullable
    OpticalRecord read(int index);

    @Nullable
    OpticalRecord read(Date date);

}

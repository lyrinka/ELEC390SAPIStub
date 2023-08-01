package app.uvtracker.data.optical.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.cache.impl.VolatileOpticalDataCacheImpl;
import app.uvtracker.sensor.pii.event.IEventSource;

public interface IOpticalDataCache extends IEventSource {

    // Emits event: OpticalDataCacheUpdateEvent

    @NonNull
    static IOpticalDataCache getVolatileImpl() {
        return new VolatileOpticalDataCacheImpl();
    }

    int getStartIndex();

    int getEndIndex();

    default int getSize() {
        if(this.getStartIndex() < 0 || this.getEndIndex() < 0) return 0;
        return this.getStartIndex() - this.getEndIndex() + 1;
    }

    @Nullable
    int[] computeRequestInterval(int first, int last);

    void writeInterval(int index, @NonNull OpticalRecord[] objects);

    @Nullable
    OpticalRecord read(int index);

}

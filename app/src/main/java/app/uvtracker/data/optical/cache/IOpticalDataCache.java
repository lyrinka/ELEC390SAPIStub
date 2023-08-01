package app.uvtracker.data.optical.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.cache.impl.VolatileOpticalDataCacheImpl;
import app.uvtracker.sensor.pii.event.IEventSource;

public interface IOpticalDataCache extends IOpticalDataCacheReader {

    @NonNull
    static IOpticalDataCache getVolatileImpl() {
        return new VolatileOpticalDataCacheImpl();
    }

    @Nullable
    int[] computeRequestInterval(int first, int last);

    void writeInterval(int index, @NonNull OpticalRecord[] objects);

}

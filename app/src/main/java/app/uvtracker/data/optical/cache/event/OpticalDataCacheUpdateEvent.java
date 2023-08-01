package app.uvtracker.data.optical.cache.event;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.cache.IOpticalDataCache;

public class OpticalDataCacheUpdateEvent {

    @NonNull
    private final IOpticalDataCache cache;

    private final int startIndex;

    private final int count;

    public OpticalDataCacheUpdateEvent(@NonNull IOpticalDataCache cache, int startIndex, int count) {
        this.cache = cache;
        this.startIndex = startIndex;
        this.count = count;
    }

    @NonNull
    public IOpticalDataCache getCache() {
        return cache;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getCount() {
        return count;
    }

}

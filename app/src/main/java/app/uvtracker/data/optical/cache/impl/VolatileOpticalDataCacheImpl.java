package app.uvtracker.data.optical.cache.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.cache.IOpticalDataCache;
import app.uvtracker.data.optical.cache.event.OpticalDataCacheUpdateEvent;
import app.uvtracker.sensor.pii.event.EventRegistry;

public class VolatileOpticalDataCacheImpl extends EventRegistry implements IOpticalDataCache {

    public static class Interval {

        public int first;
        public int last;

        public Interval() {
            this.first = -1;
            this.last = -1;
        }

        public Interval(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public boolean uninitialized() {
            return this.first < 0 || this.last < 0;
        }

    }

    @NonNull
    private final Interval interval;

    @NonNull
    private final HashMap<Integer, OpticalRecord> storage;

    public VolatileOpticalDataCacheImpl() {
        this.interval = new Interval();
        this.storage = new HashMap<>();
    }

    @Override
    public int getStartIndex() {
        return this.interval.first;
    }

    @Override
    public int getEndIndex() {
        return this.interval.last;
    }

    @Nullable
    private Interval computeRequestIntervalCore(Interval input) {
        if(this.interval.uninitialized()) return input;
        if(input.last > this.interval.last) {
            input.first = this.interval.last + 1;
            return input;
        }
        if(input.first < this.interval.first) {
            input.last = this.interval.first - 1;
            return input;
        }
        return null;
    }

    @Nullable
    @Override
    public int[] computeRequestInterval(int first, int last) {
        Interval interval = computeRequestIntervalCore(new Interval(first, last));
        if(interval == null) return null;
        return new int[]{interval.first, interval.last};
    }

    @Override
    public void writeInterval(int first, @NonNull OpticalRecord[] objects) {
        if(objects.length == 0) return;
        int last = first + objects.length - 1;
        if(this.interval.uninitialized()) {
            this.interval.first = first;
            this.interval.last = last;
        }
        else {
            if(first < this.interval.first) this.interval.first = first;
            if(last > this.interval.last) this.interval.last = last;
        }
        int index = first;
        for(OpticalRecord object : objects) {
            this.storage.putIfAbsent(index++, object);
        }
        this.dispatch(new OpticalDataCacheUpdateEvent(this, first, objects.length));
    }

    @Override
    @Nullable
    public OpticalRecord read(int index) {
        return this.storage.get(index);
    }

}

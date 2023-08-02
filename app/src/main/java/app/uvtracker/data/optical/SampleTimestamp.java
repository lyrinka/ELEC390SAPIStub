package app.uvtracker.data.optical;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;

public class SampleTimestamp {

    public final int year;
    public final int month;
    public final int date;

    @NonNull
    public final Date day;

    @NonNull
    public final Date instant;

    public final int sampleNumber;
    public final int sampleInterval;

    public SampleTimestamp(int year, int month, int date, int sampleNumber, int sampleInterval) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.day = new Date(year - 1900, month - 1, date);
        this.instant = new Date(day.getTime() + (long)sampleNumber * sampleInterval * 1000);
        this.sampleNumber = sampleNumber;
        this.sampleInterval = sampleInterval;
    }

    public SampleTimestamp(long deviceBootTime, int globalSampleNumber, int sampleInterval) {
        this.sampleInterval = sampleInterval;
        this.instant = new Date(deviceBootTime + (long)globalSampleNumber * sampleInterval * 1000);
        this.year = this.instant.getYear() + 1900;
        this.month = this.instant.getMonth() + 1;
        this.date = this.instant.getDate();
        this.day = new Date(this.year - 1900, this.month - 1, this.date);
        int midnightSampleNumber = (int)Math.ceil((double)(this.day.getTime() - deviceBootTime) / (double)(sampleInterval * 1000));
        this.sampleNumber = globalSampleNumber - midnightSampleNumber;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("%d-%02d-%02d (%d)", this.year, this.month, this.date, this.sampleNumber);
    }

}

package app.uvtracker.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Map;

import app.uvtracker.data.type.OpticalRecord;

public interface IPersistentDataAccess {

    void clearDB();

    void writeHourlyAverage(@NonNull Date time, @NonNull OpticalRecord record);

    @Nullable
    OpticalRecord readHourlyAverage(@NonNull Date time);

    @NonNull
    Map<Date, OpticalRecord> readAllHourlyAverage();

}

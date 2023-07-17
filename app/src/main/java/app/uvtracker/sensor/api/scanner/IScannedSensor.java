package app.uvtracker.sensor.api.scanner;

import androidx.annotation.NonNull;

import java.util.Date;

import app.uvtracker.sensor.api.Old_ISensor;

public interface IScannedSensor {

    @NonNull
    Old_ISensor getSensor();

    int getRssi();

    @NonNull
    Date lastSeenAt();

}

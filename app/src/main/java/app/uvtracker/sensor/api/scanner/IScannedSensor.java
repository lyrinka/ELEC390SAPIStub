package app.uvtracker.sensor.api.scanner;

import androidx.annotation.NonNull;

import java.util.Date;

import app.uvtracker.sensor.api.ISensor;

public interface IScannedSensor {

    @NonNull
    ISensor getSensor();

    int getRssi();

    @NonNull
    Date lastSeenAt();

}

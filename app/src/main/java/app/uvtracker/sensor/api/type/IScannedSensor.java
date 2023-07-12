package app.uvtracker.sensor.api.type;

import androidx.annotation.NonNull;

import java.util.Date;

public interface IScannedSensor {

    @NonNull
    AndroidBLESensor getSensor();

    int getRssi();

    @NonNull
    Date lastSeenAt();

}

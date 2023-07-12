package app.uvtracker.sensor.api.scanner;

import androidx.annotation.NonNull;

import java.util.Collection;

public interface IScannerCallback {

    void onScanUpdate(@NonNull IScannedSensor sensor, boolean isFirstTime, @NonNull Collection<? extends IScannedSensor> sensors);

}

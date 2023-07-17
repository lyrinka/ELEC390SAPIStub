package app.uvtracker.sensor.pdi.android.scanner;

import android.bluetooth.le.ScanResult;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

import app.uvtracker.sensor.api.scanner.IScannedSensor;
import app.uvtracker.sensor.api.Old_ISensor;
import app.uvtracker.sensor.pdi.android.old.Old_AndroidBLESensor;

public class AndroidBLEScannedSensor implements IScannedSensor {

    @NonNull
    private final Old_ISensor sensor;

    private int rssi;

    @NonNull
    private Date timestamp;

    public AndroidBLEScannedSensor(@NonNull ScanResult result, @NonNull Context context) {
        this(new Old_AndroidBLESensor(result.getDevice(), context), result.getRssi());
    }

    public AndroidBLEScannedSensor(@NonNull Old_ISensor sensor, int rssi) {
        this.sensor = sensor;
        this.rssi = rssi;
        this.timestamp = new Date();
    }

    @NonNull
    @Override
    public Old_ISensor getSensor() {
        return this.sensor;
    }

    @Override
    public int getRssi() {
        return this.rssi;
    }

    @NonNull
    @Override
    public Date lastSeenAt() {
        return this.timestamp;
    }

    public void updateFromResult(@NonNull ScanResult result) {
        this.rssi = result.getRssi();
        this.timestamp = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AndroidBLEScannedSensor)) return false;
        AndroidBLEScannedSensor that = (AndroidBLEScannedSensor) o;
        return sensor.equals(that.sensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensor);
    }

}

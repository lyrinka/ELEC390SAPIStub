package app.uvtracker.sensor.pdi.android.type;

import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

import app.uvtracker.sensor.api.type.IScannedSensor;
import app.uvtracker.sensor.api.type.AndroidBLESensor;

public class AndroidBLEScannedSensor implements IScannedSensor {

    @NonNull
    private final AndroidBLESensor sensor;

    private int rssi;

    @NonNull
    private Date timestamp;

    public AndroidBLEScannedSensor(@NonNull ScanResult result) {
        this(new app.uvtracker.sensor.pdi.android.type.AndroidBLESensor(result.getDevice()), result.getRssi());
    }

    public AndroidBLEScannedSensor(@NonNull AndroidBLESensor sensor, int rssi) {
        this.sensor = sensor;
        this.rssi = rssi;
        this.timestamp = new Date();
    }

    @NonNull
    @Override
    public AndroidBLESensor getSensor() {
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

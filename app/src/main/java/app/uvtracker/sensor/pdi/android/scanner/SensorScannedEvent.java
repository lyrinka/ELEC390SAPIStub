package app.uvtracker.sensor.pdi.android.scanner;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import java.util.Collection;

public class SensorScannedEvent {

    @NonNull
    private final BluetoothDevice sensor;

    private final boolean isFirstTime;

    @NonNull
    private final Collection<BluetoothDevice> sensors;

    public SensorScannedEvent(@NonNull BluetoothDevice sensor, boolean isFirstTime, @NonNull Collection<BluetoothDevice> sensors) {
        this.sensor = sensor;
        this.isFirstTime = isFirstTime;
        this.sensors = sensors;
    }

    @NonNull
    public BluetoothDevice getSensor() {
        return sensor;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    @NonNull
    public Collection<BluetoothDevice> getSensors() {
        return sensors;
    }

}

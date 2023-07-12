package app.uvtracker.sensor.api;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.api.exception.BluetoothException;
import app.uvtracker.sensor.api.type.IScannerCallback;

public interface IScanner {

    void startScanning(@NonNull IScannerCallback consumer) throws BluetoothException;

    void stopScanning() throws BluetoothException;

    boolean isScanning();

}

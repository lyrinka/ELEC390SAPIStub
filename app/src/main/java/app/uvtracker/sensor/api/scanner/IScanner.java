package app.uvtracker.sensor.api.scanner;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.api.exception.TransceiverException;

public interface IScanner {

    void startScanning(@NonNull IScannerCallback consumer) throws TransceiverException;

    void stopScanning() throws TransceiverException;

    boolean isScanning();

}

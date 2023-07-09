package com.example.elec390.sapi;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ISensorManager {

    default void startScanning(@NonNull Consumer<SensorInfo> updateCallback) {
        this.startScanning(updateCallback, (s) -> s.name != null);
    }
    void startScanning(@NonNull Consumer<SensorInfo> updateCallback, @NonNull Predicate<SensorInfo> filter);
    void stopScanning();
    boolean isScanning();
    @NonNull List<SensorInfo> getScanResults();


    // WIP
    default ISensor getSensor(@NonNull SensorInfo info) {
        return this.getSensor(info.address);
    }
    ISensor getSensor(@NonNull String address);

}

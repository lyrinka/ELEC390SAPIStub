package com.example.elec390.sapi;

import androidx.annotation.NonNull;

import com.example.elec390.sapi.data.DataSample;
import com.example.elec390.sapi.type.SensorButton;
import com.example.elec390.sapi.type.SensorButtonAction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ISensor {
    @NonNull
    SensorInfo getBaseInfo();

    // Fundamental
    void connect();
    boolean isConnected();
    void disconnect();

    // System
    void onDisconnection(Runnable callback);
/*
    // UI
    void flash();
    void beep();
    void onButtonInteraction(BiConsumer<SensorButton, SensorButtonAction> callback);

    // Real-time DAQ
    void onNewSample(Consumer<DataSample> callback);
*/
}

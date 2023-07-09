package com.example.elec390.sapi;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class SensorInfo {

    public final String name;
    public final String address;

    public SensorInfo(@Nullable String name, @NonNull String address) {
        this.name = name;
        this.address = address;
    }

    @SuppressLint("MissingPermission")
    public static SensorInfo fromBluetoothDevice(BluetoothDevice device) {
        return new SensorInfo(device.getName(), device.getAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensorInfo)) return false;
        SensorInfo sensor = (SensorInfo) o;
        return address.equals(sensor.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @NonNull
    @Override
    public String toString() {
        return "ScannedSensor{" + address + (name == null ? "}" : ", " + name + "}");
    }
}

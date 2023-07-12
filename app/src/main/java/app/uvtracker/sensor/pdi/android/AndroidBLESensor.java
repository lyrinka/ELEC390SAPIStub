package app.uvtracker.sensor.pdi.android;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.uvtracker.sensor.api.ISensor;

public class AndroidBLESensor implements ISensor {

    @NonNull
    private final BluetoothDevice device;

    @Nullable
    private final String name;

    @SuppressLint("MissingPermission")
    public AndroidBLESensor(@NonNull BluetoothDevice device) {
        this.device = device;
        this.name = device.getName();
    }

    @NonNull
    @Override
    public String getAddress() {
        return this.device.getAddress();
    }

    @Nullable
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AndroidBLESensor)) return false;
        AndroidBLESensor that = (AndroidBLESensor) o;
        return this.device.getAddress().equals(that.device.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.device.getAddress());
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect() {

    }

}

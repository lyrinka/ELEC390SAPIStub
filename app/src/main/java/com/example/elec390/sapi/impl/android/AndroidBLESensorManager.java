package com.example.elec390.sapi.impl.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.elec390.sapi.ISensor;
import com.example.elec390.sapi.SensorInfo;
import com.example.elec390.sapi.ISensorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AndroidBLESensorManager implements ISensorManager {

    private final BluetoothAdapter bluetoothAdapter;

    private boolean isScanning = false;
    private ManagerScanCallback internalCallback;

    public AndroidBLESensorManager(@NonNull Activity activity) {
        BluetoothManager bluetoothManager = activity.getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) throw new RuntimeException("Bluetooth not supported");
        if (!this.bluetoothAdapter.isEnabled()) throw new RuntimeException("Bluetooth off");

    }

    @Override
    public void startScanning(@NonNull Consumer<SensorInfo> updateCallback, @NonNull Predicate<SensorInfo> filter) {
        if(this.isScanning) return;
        this.internalCallback = new ManagerScanCallback(updateCallback, filter);
        this.startScanning();
    }

    @SuppressLint("MissingPermission")
    private void startScanning() {
        this.isScanning = true;
        this.bluetoothAdapter.getBluetoothLeScanner().startScan(this.internalCallback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopScanning() {
        if(!this.isScanning) return;
        this.bluetoothAdapter.getBluetoothLeScanner().stopScan(this.internalCallback);
        this.isScanning = false;
    }

    @Override
    public boolean isScanning() {
        return this.isScanning;
    }

    @Override
    public @Nullable List<SensorInfo> getScanResults() {
        if(this.internalCallback == null) return new ArrayList<>();
        return this.internalCallback.getSnapshot();
    }

    @Override
    public ISensor getSensor(@NonNull String address) {
        return null;
    }

}

class ManagerScanCallback extends ScanCallback {

    private final Consumer<SensorInfo> consumer;
    private final Predicate<SensorInfo> filter;

    private final List<SensorInfo> list = new ArrayList<>();

    public ManagerScanCallback(@NonNull Consumer<SensorInfo> consumer, @NonNull Predicate<SensorInfo> filter) {
        this.consumer = consumer;
        this.filter = filter;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        SensorInfo sensor = SensorInfo.fromBluetoothDevice(result.getDevice());
        if(!this.filter.test(sensor)) return;
        if(this.list.contains(sensor)) return;
        this.list.add(sensor);
        this.consumer.accept(sensor);
    }

    public List<SensorInfo> getSnapshot() {
        return new ArrayList<>(this.list);
    }

}

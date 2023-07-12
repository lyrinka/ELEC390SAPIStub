package app.uvtracker.sensor.pdi.android.scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import app.uvtracker.sensor.api.scanner.IScanner;
import app.uvtracker.sensor.api.exception.BluetoothException;
import app.uvtracker.sensor.api.exception.BluetoothNoPermException;
import app.uvtracker.sensor.api.exception.BluetoothOffException;
import app.uvtracker.sensor.api.exception.BluetoothUnsupportedException;
import app.uvtracker.sensor.api.scanner.IScannerCallback;
import app.uvtracker.sensor.pdi.BLEDeviceDesc;

public class AndroidBLEScanner implements IScanner {

    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    @Nullable
    private final BooleanSupplier permissionChecker;

    private boolean isScanning = false;

    @Nullable
    private InternalScanCallback callback;

    public AndroidBLEScanner(@NonNull Activity activity) throws BluetoothException {
        this(activity, null);
    }

    public AndroidBLEScanner(@NonNull Activity activity, @Nullable BooleanSupplier permissionChecker) throws BluetoothException {
        this.permissionChecker = permissionChecker;

        BluetoothManager bluetoothManager = activity.getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) throw new BluetoothUnsupportedException();
        if (!this.bluetoothAdapter.isEnabled()) throw new BluetoothOffException();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void startScanning(@NonNull IScannerCallback consumer) throws BluetoothException {
        if (this.isScanning) return;
        if(this.permissionChecker != null && !this.permissionChecker.getAsBoolean()) throw new BluetoothNoPermException();

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
        if(BLEDeviceDesc.RESTRICTED)
            filterBuilder.setServiceUuid(ParcelUuid.fromString(BLEDeviceDesc.SERVICE_UUID));
        filters.add(filterBuilder.build());
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        this.callback = new InternalScanCallback(consumer);

        this.isScanning = true;
        this.bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, this.callback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopScanning() throws BluetoothException {
        if (!this.isScanning) return;
        if(this.permissionChecker != null && !this.permissionChecker.getAsBoolean()) throw new BluetoothNoPermException();
        this.bluetoothAdapter.getBluetoothLeScanner().stopScan(this.callback);
        this.isScanning = false;
    }

    @Override
    public boolean isScanning() {
        return this.isScanning;
    }

}

class InternalScanCallback extends ScanCallback {

    @NonNull
    private final HashMap<String, AndroidBLEScannedSensor> map;

    @NonNull
    private final IScannerCallback callback;

    public InternalScanCallback(@NonNull IScannerCallback callback) {
        this.map = new HashMap<>();
        this.callback = callback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        AndroidBLEScannedSensor scannedSensor = new AndroidBLEScannedSensor(result);
        String address = scannedSensor.getSensor().getAddress();

        boolean firstTime = !this.map.containsKey(address);
        if(firstTime)
            this.map.put(address, scannedSensor);
        else
            Objects.requireNonNull(this.map.get(address)).updateFromResult(result);

        this.callback.onScanUpdate(scannedSensor, firstTime, this.map.values());
    }

}
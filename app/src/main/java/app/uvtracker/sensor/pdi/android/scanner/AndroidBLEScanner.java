package app.uvtracker.sensor.pdi.android.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import app.uvtracker.sensor.api.scanner.IScanner;
import app.uvtracker.sensor.api.exception.transceiver.TransceiverException;
import app.uvtracker.sensor.api.exception.transceiver.TransceiverNoPermException;
import app.uvtracker.sensor.api.exception.transceiver.TransceiverOffException;
import app.uvtracker.sensor.api.exception.transceiver.TransceiverUnsupportedException;
import app.uvtracker.sensor.api.scanner.IScannerCallback;
import app.uvtracker.sensor.pdi.BLEDeviceDesc;

public class AndroidBLEScanner implements IScanner {

    @NonNull
    private final Context context;

    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    @Nullable
    private final BooleanSupplier permissionChecker;

    private boolean isScanning = false;

    @Nullable
    private InternalScanCallback callback;

    public AndroidBLEScanner(@NonNull Context activity) throws TransceiverException {
        this(activity, null);
    }

    public AndroidBLEScanner(@NonNull Context context, @Nullable BooleanSupplier permissionChecker) throws TransceiverException {
        this.permissionChecker = permissionChecker;
        this.context = context;

        BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) throw new TransceiverUnsupportedException();
        if (!this.bluetoothAdapter.isEnabled()) throw new TransceiverOffException();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void startScanning(@NonNull IScannerCallback consumer) throws TransceiverException {
        if (this.isScanning) return;
        if(this.permissionChecker != null && !this.permissionChecker.getAsBoolean()) throw new TransceiverNoPermException();

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
        if(BLEDeviceDesc.RESTRICTED)
            filterBuilder.setServiceUuid(ParcelUuid.fromString(BLEDeviceDesc.SERVICE_UUID));
        filters.add(filterBuilder.build());
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        this.callback = new InternalScanCallback(consumer, this.context);

        this.isScanning = true;
        this.bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, this.callback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopScanning() throws TransceiverException {
        if (!this.isScanning) return;
        if(this.permissionChecker != null && !this.permissionChecker.getAsBoolean()) throw new TransceiverNoPermException();
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

    @NonNull
    private final Context context;

    public InternalScanCallback(@NonNull IScannerCallback callback, Context context) {
        this.map = new HashMap<>();
        this.callback = callback;
        this.context = context;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        AndroidBLEScannedSensor scannedSensor = new AndroidBLEScannedSensor(result, this.context);
        String address = scannedSensor.getSensor().getAddress();

        boolean firstTime = !this.map.containsKey(address);
        if(firstTime)
            this.map.put(address, scannedSensor);
        else
            Objects.requireNonNull(this.map.get(address)).updateFromResult(result);

        this.callback.onScanUpdate(scannedSensor, firstTime, this.map.values());
    }

}
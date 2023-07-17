package app.uvtracker.sensor.pdi.android.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import app.uvtracker.sensor.api.event.EventRegistry;
import app.uvtracker.sensor.api.exception.TransceiverException;
import app.uvtracker.sensor.api.exception.TransceiverNoPermException;
import app.uvtracker.sensor.api.exception.TransceiverOffException;
import app.uvtracker.sensor.api.exception.TransceiverUnsupportedException;
import app.uvtracker.sensor.pii.connection.BLEOptions;

public class AndroidBLEScanner extends EventRegistry {

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
    public void startScanning() throws TransceiverException {
        if (this.isScanning) return;
        if(this.permissionChecker != null && !this.permissionChecker.getAsBoolean()) throw new TransceiverNoPermException();

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
        if(BLEOptions.Scanner.RESTRICTED)
            filterBuilder.setServiceUuid(new ParcelUuid(BLEOptions.Scanner.FILTER_UUID));
        filters.add(filterBuilder.build());
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        this.callback = new InternalScanCallback(this::dispatch, this.context);

        this.isScanning = true;
        this.bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, this.callback);
    }

    @SuppressLint("MissingPermission")
    public void stopScanning() throws TransceiverException {
        if (!this.isScanning) return;
        if(this.permissionChecker != null && !this.permissionChecker.getAsBoolean()) throw new TransceiverNoPermException();
        this.bluetoothAdapter.getBluetoothLeScanner().stopScan(this.callback);
        this.isScanning = false;
    }

    public boolean isScanning() {
        return this.isScanning;
    }

}

class InternalScanCallback extends ScanCallback {

    @NonNull
    private final HashMap<String, BluetoothDevice> map;

    @NonNull
    private final Consumer<SensorScannedEvent> callback;

    @NonNull
    private final Context context;

    public InternalScanCallback(@NonNull Consumer<SensorScannedEvent> callback, @NonNull Context context) {
        this.map = new HashMap<>();
        this.callback = callback;
        this.context = context;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);

        BluetoothDevice device = result.getDevice();
        String address = device.getAddress();

        boolean firstTime = !this.map.containsKey(address);
        if(firstTime) this.map.put(address, device);

        this.callback.accept(new SensorScannedEvent(device, firstTime, this.map.values()));
    }

}
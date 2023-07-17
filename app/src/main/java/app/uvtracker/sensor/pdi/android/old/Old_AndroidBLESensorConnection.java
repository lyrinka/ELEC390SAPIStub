package app.uvtracker.sensor.pdi.android.old;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import app.uvtracker.sensor.api.Old_ISensor;
import app.uvtracker.sensor.pdi.BLEDeviceDesc;
import app.uvtracker.sensor.pdi.android.util.CancellableDelayedOneTimeTask;

@SuppressLint("MissingPermission")
public class Old_AndroidBLESensorConnection extends BluetoothGattCallback {

    @NonNull
    private static final String TAG = Old_AndroidBLESensorConnection.class.getSimpleName();

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
    private static int CONNECTION_TIMEOUT = 8000;

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
    private static int CONNECTION_GRACE_PERIOD = 500;

    public enum Stage {
        IDLE,
        CONNECTING,
        DISCOVERING_SERVICE,
        REQUESTING_MTU,
        ENABLING_NOTIFICATION,
        WAITING_GRACE_PERIOD,
        ESTABLISHED,
        DISCONNECTING,
    }

    @NonNull
    private Stage stage;

    @NonNull
    private final Consumer<Old_ISensor.ConnectionStatus> statusCallback;

    @NonNull
    private final Consumer<byte[]> dataCallback;

    @NonNull
    private final Old_AndroidBLESensor sensor;

    @Nullable
    private BluetoothGatt gatt;

    @Nullable
    private BluetoothGattCharacteristic readHandle;

    @Nullable
    private BluetoothGattCharacteristic writeHandle;

    @Nullable
    private CancellableDelayedOneTimeTask delayedTask;

    private int sessionMTU = 0;

    private int sessionWriteType;

    public Old_AndroidBLESensorConnection(@NonNull Old_AndroidBLESensor sensor,
                                          @NonNull Consumer<Old_ISensor.ConnectionStatus> statusCallback,
                                          @NonNull Consumer<byte[]> dataCallback) {
        this.stage = Stage.IDLE;
        this.sensor = sensor;
        this.statusCallback = statusCallback;
        this.dataCallback = dataCallback;
    }

    private void setStage(Stage stage) {
        Log.d(TAG, ">> Stage changed from " + this.stage + " to " + stage);
        this.stage = stage;
    }

    @NonNull
    public Stage getStage() {
        return this.stage;
    }

    public boolean isConnected() {
        return this.stage == Stage.ESTABLISHED;
    }

    // Connection flow
    public boolean connect() {
        Log.d(TAG, "connect()");
        if(this.stage != Stage.IDLE) {
            Log.d(TAG, "- Operations ongoing..");
            return false;
        }
        Log.d(TAG, "- Initiating connection..");
        this.gatt = this.sensor.getPlatformDevice()
                .connectGatt(
                        this.sensor.getPlatformContext(),
                        false,
                        this,
                        BluetoothDevice.TRANSPORT_LE
                );
        this.setStage(Stage.CONNECTING);
        this.setDelayedTask(new CancellableDelayedOneTimeTask(() -> {
            Log.d(TAG, ">> Application-level timeout.");
            this.reset();
        }).post(CONNECTION_TIMEOUT));
        return true;
    }

    public boolean disconnect() {
        Log.d(TAG, "disconnect()");
        if(this.stage == Stage.IDLE) {
            Log.d(TAG, "- Connection inactive..");
            return false;
        }
        Log.d(TAG, "- Initiating disconnection..");
        Objects.requireNonNull(this.gatt).disconnect();
        this.setStage(Stage.DISCONNECTING);
        this.cancelDelayedTask();
        return true;
    }

    public void reset() {
        Log.d(TAG, "reset()");
        if(this.gatt != null) {
            this.gatt.disconnect();
            this.gatt.close();
            this.gatt = null;
            Log.d(TAG, "- Force reset performed.");
        }
        this.setStage(Stage.IDLE);
        this.cancelDelayedTask();
        this.statusCallback.accept(Old_ISensor.ConnectionStatus.FAILED_RETRY);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(TAG, "onConnectionStateChange() [override]");
        this.gatt = gatt;
        if(status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "- Success. New state: " + newState);
            switch(newState) {
                case BluetoothGatt.STATE_CONNECTED: {
                    Log.d(TAG, "- Connected! Initiating service discovery...");
                    this.setStage(Stage.DISCOVERING_SERVICE);
                    (new Handler(Looper.getMainLooper()))   // Trigger service discovery in main thread
                            .post(() -> Objects.requireNonNull(this.gatt).discoverServices());
                    break;
                }
                case BluetoothGatt.STATE_DISCONNECTED: {
                    Log.d(TAG, "- Disconnected.");
                    this.gracefulClose();
                    break;
                }
            }
        }
        else {
            Log.d(TAG, "- Failed. Status code: " + status);
            this.gracefulClose(true, true);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(TAG, "onServicesDiscovered() [override]");
        Objects.requireNonNull(this.gatt);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "- Success!");
            BluetoothGattService service = gatt.getService(UUID.fromString(BLEDeviceDesc.SERVICE_UUID));
            if(service == null) {
                Log.d(TAG, "- Failed: device does not contain necessary service.");
                this.gracefulClose(true, false);
                return;
            }
            BluetoothGattCharacteristic readHandle = service.getCharacteristic(UUID.fromString(BLEDeviceDesc.READ_CHARACTERISTIC_UUID));
            BluetoothGattCharacteristic writeHandle = service.getCharacteristic(UUID.fromString(BLEDeviceDesc.WRITE_CHARACTERISTIC_UUID));
            if(readHandle == null || writeHandle == null) {
                Log.d(TAG, "- Failed: service does not contain necessary characteristics.");
                this.gracefulClose(true, false);
                return;
            }
            if((readHandle.getProperties() & BLEDeviceDesc.READ_CHARACTERISTIC_PROPERTY) == 0) {
                Log.d(TAG, "- Failed: read endpoint does not have necessary property.");
                this.gracefulClose(true, false);
                return;
            }
            if((writeHandle.getProperties() & BLEDeviceDesc.WRITE_CHARACTERISTIC_PROPERTY) == 0) {
                Log.d(TAG, "- Failed: read endpoint does not have necessary property.");
                this.gracefulClose(true, false);
                return;
            }
            this.readHandle = readHandle;
            this.writeHandle = writeHandle;
            this.sessionWriteType = BLEDeviceDesc.WRITE_CHARACTERISTIC_TYPE;
            // Request MTU
            this.gatt.requestMtu(BLEDeviceDesc.REQUEST_MTU);
            this.setStage(Stage.REQUESTING_MTU);
        }
        else {
            Log.d(TAG, "- Failed. Status code: " + status);
            this.gracefulClose(true, true);
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        Log.d(TAG, "onMtuChanged() [override]");
        Objects.requireNonNull(this.gatt);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "- Success! New MTU: " + mtu);
            if(mtu != BLEDeviceDesc.REQUEST_MTU) {
                Log.d(TAG, "- MTU request failed.");
                this.gracefulClose(true, false);
            }
            this.sessionMTU = mtu;
            Log.d(TAG, "- Enabling read notification...");
            Objects.requireNonNull(this.readHandle);
            if(!this.gatt.setCharacteristicNotification(this.readHandle, true)) {
                Log.d(TAG, "- Failed: Failed to set read notification.");
                this.gracefulClose(true, false);
                return;
            }
            Log.d(TAG, "- Set characteristic notification.");
            BluetoothGattDescriptor descriptor = Objects.requireNonNull(this.readHandle.getDescriptor(UUID.fromString(BLEDeviceDesc.CCCD_UUID)));
            descriptor.setValue(BLEDeviceDesc.READ_CHARACTERISTIC_CCCD_VAL);
            this.gatt.writeDescriptor(descriptor);
            Log.d(TAG, "- Initiated descriptor write.");
            this.setStage(Stage.ENABLING_NOTIFICATION);
        }
        else {
            Log.d(TAG, "- Failed. Status code: " + status);
            this.gracefulClose(true, true);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        Log.d(TAG, "onDescriptorWrite() [override]");
        Objects.requireNonNull(this.gatt);
        if(descriptor == null
                || !descriptor.getCharacteristic().getUuid()
                        .equals(Objects.requireNonNull(this.readHandle).getUuid())
                || !descriptor.getUuid()
                        .equals(UUID.fromString(BLEDeviceDesc.CCCD_UUID))) {
            Log.d(TAG, "- (Some other descriptor changed)");
            return;
        }
        if(status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "- Confirmed notification setup. Entering grace period...");
            this.setDelayedTask(new CancellableDelayedOneTimeTask(this::onLinkLayerEstablishDelayed).post(CONNECTION_GRACE_PERIOD));
            this.setStage(Stage.WAITING_GRACE_PERIOD);
        }
        else {
            Log.d(TAG, "- Failed. Status code: " + status);
            this.gracefulClose(true, true);
        }
    }

    private void onLinkLayerEstablishDelayed() {
        Log.d(TAG, "onLinkLayerEstablishDelayed()");
        this.cancelDelayedTask();
        this.setStage(Stage.ESTABLISHED);
        Log.d(TAG, "- Connection established! We're all done.");
        this.statusCallback.accept(Old_ISensor.ConnectionStatus.ESTABLISHED);
    }

    // Data exchange
    public int getSessionMTU() {
        return this.sessionMTU < 23 ? -1 : this.sessionMTU - 3;
    }

    public boolean write(String data) {
        // TODO: better conversion of non-printable ASCII characters?
        String readableData = data.trim();
        Log.d(TAG, "write(\"" + readableData + "\")");
        if(!this.isConnected()) {
            Log.d(TAG, "- Connection not established. Write ignored.");
            return false;
        }
        Objects.requireNonNull(this.writeHandle);
        byte[] bytes = data.getBytes(StandardCharsets.US_ASCII);
        if(bytes.length > this.sessionMTU - 3) {
            // TODO: better MTU handling? Data fragmentation & queueing?
            Log.d(TAG, "- MTU exceeded: " + bytes.length + " exceeded MTU " + this.sessionMTU + " - 3. Truncating input...");
            bytes = Arrays.copyOf(bytes, this.sessionMTU - 3);
        }
        this.writeHandle.setWriteType(this.sessionWriteType);
        this.writeHandle.setValue(bytes);
        Objects.requireNonNull(this.gatt).writeCharacteristic(this.writeHandle);
        Log.d(TAG, "- Written " + bytes.length + " bytes.");
        return true;
    }

    @Override
    public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        Log.d(TAG, "onCharacteristicChanged() [override]");
        if(!characteristic.getUuid().equals(Objects.requireNonNull(this.readHandle).getUuid())) {
            Log.d(TAG, "- (Some other characteristic changed)");
            return;
        }
        byte[] value = Objects.requireNonNull(characteristic.getValue());
        // TODO: Data queueing? Move operation to main thread?
        Log.d(TAG, "Received value(s): " + new String(value, StandardCharsets.US_ASCII));
        this.dataCallback.accept(value);
    }

    // Internal helpers
    private void gracefulClose() {
        this.gracefulClose(false, false);
    }

    private void gracefulClose(boolean failed, boolean retryAdvised) {
        if(!failed) Log.d(TAG, ">> Graceful close helper: closing...");
        else Log.d(TAG, ">> Graceful close helper: Closing due to failure...");
        Objects.requireNonNull(this.gatt).close();
        this.setStage(Stage.IDLE);
        this.cancelDelayedTask();
        this.statusCallback.accept(
                !failed ? Old_ISensor.ConnectionStatus.DISCONNECTED
                        : retryAdvised ? Old_ISensor.ConnectionStatus.FAILED_RETRY
                                       : Old_ISensor.ConnectionStatus.FAILED_NO_RETRY
        );
    }

    private void cancelDelayedTask() {
        if(this.delayedTask != null) this.delayedTask.cancel();
    }

    private void setDelayedTask(CancellableDelayedOneTimeTask task) {
        if(this.delayedTask != null) this.delayedTask.cancel();
        this.delayedTask = task;
    }

}


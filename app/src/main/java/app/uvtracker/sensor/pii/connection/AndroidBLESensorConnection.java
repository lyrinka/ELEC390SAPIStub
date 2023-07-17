package app.uvtracker.sensor.pii.connection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.uvtracker.sensor.api.event.EventRegistry;

@SuppressLint("MissingPermission")
public class AndroidBLESensorConnection extends EventRegistry implements ISensorConnection {

    @NonNull
    private static final String TAG = AndroidBLESensorConnection.class.getSimpleName();

    @NonNull
    private final Handler handler;

    @NonNull
    private final Context context;

    @Nullable
    private DelayedTask delayedTask;

    @NonNull
    private final BluetoothDevice device;

    @NonNull
    private Stage internalStage;

    @NonNull
    private final BluetoothGattCallbackImpl bleCallback;

    @Nullable
    private BluetoothGatt gattObj;

    @Nullable
    private CharacteristicStore endpoints;

    public AndroidBLESensorConnection(@NonNull BluetoothDevice device, @NonNull Context context) {
        this.handler = new Handler(Looper.getMainLooper()); // TODO: which thread to use?
        this.context = context;
        this.device = device;
        this.bleCallback = new BluetoothGattCallbackImpl(this);
        this.internalStage = Stage.DISCONNECTED;
    }

    private void debug(@NonNull String msg, Object... args) {
        if(BLEOptions.TRACE_ENABLED) Log.d(TAG, String.format(msg, args));
    }

    private BluetoothGatt getGatt() {
        return Objects.requireNonNull(this.gattObj);
    }

    private CharacteristicStore getEndpoints() {
        return Objects.requireNonNull(this.endpoints);
    }

    private void wipeReferences() {
        this.gattObj = null;
        this.endpoints = null;
    }

    private void setStage(@NonNull Stage stage) {
        this.debug(">> New stage: %1$s", stage);
        this.internalStage = stage;
    }

    private boolean ensureStage(Stage... stages) {
        for(Stage stage : stages) {
            if(this.internalStage == stage)
                return true;
        }
        return false;
    }

    protected void postTask(@NonNull Runnable r) {
        this.handler.post(r);
    }

    protected void setDelayedTask(@NonNull Runnable r, int delayms) {
        this.cancelDelayedTask();
        this.delayedTask = new DelayedTask(r);
        this.handler.postDelayed(this.delayedTask, delayms);
    }

    protected void cancelDelayedTask() {
        if(this.delayedTask != null)
            this.delayedTask.cancel();
    }

    private void dispatch(ConnectionStageChangeEvent.Stage stage) {
        this.dispatch(new ConnectionStageChangeEvent(stage, this.internalStage.getPercentage()));
    }

    @Override
    public void reset() {
        this.debug("reset()");
        this.cancelDelayedTask();
        if(this.gattObj != null) {
            this.getGatt().disconnect();
            this.getGatt().close();
            this.debug("- Force closed everything");
        }
        this.wipeReferences();
        this.setStage(Stage.CRASHED);
        this.dispatch(ConnectionStageChangeEvent.Stage.FAILED_RETRY);
    }

    @Override
    public boolean connect() {
        this.debug("connect()");
        if(!this.ensureStage(Stage.DISCONNECTED, Stage.CRASHED)) {
            this.debug("- Request ignored.");
            return false;
        }
        this.device.connectGatt(this.context, false, this.bleCallback, BluetoothDevice.TRANSPORT_LE);
        this.setStage(Stage.CONNECTING);
        this.setDelayedTask(this::reset, BLEOptions.Connection.CONNECTION_TIMEOUT);
        this.dispatch(ConnectionStageChangeEvent.Stage.CONNECTING);
        return true;
    }

    @Override
    public boolean disconnect() {
        this.debug("disconnect()");
        if(this.ensureStage(Stage.DISCONNECTING, Stage.DISCONNECTED, Stage.CRASHED)) {
            this.debug("- Request ignored.");
            return false;
        }
        if(this.gattObj != null) {
            this.getGatt().disconnect();
            this.setStage(Stage.DISCONNECTING);
            this.setDelayedTask(this::reset, BLEOptions.Connection.DISCONNECTION_TIMEOUT);
            this.dispatch(ConnectionStageChangeEvent.Stage.DISCONNECTING);
        }
        else {
            this.gracefullyClose(true, true);
        }
        return true;
    }

    protected void onDeviceConnected(BluetoothGatt gatt) {
        this.debug("onDeviceConnected() [EV]");
        // TODO: when the device turns on after application-level timeout, the device still gets connected.
        this.gattObj = gatt;
        this.getGatt().requestMtu(BLEOptions.Device.REQUEST_MTU);
        this.setStage(Stage.REQUESTING_MTU);
        this.dispatch(ConnectionStageChangeEvent.Stage.CONNECTING);
    }

    protected void onDeviceMTURequested(int mtu) {
        this.debug("onDeviceMTURequested() [EV]");
        if(mtu < BLEOptions.Device.REQUIRE_MTU) {
            this.debug("- Requested MTU not large enough.");
            this.gracefullyClose(true, false);
        }
        this.getGatt().discoverServices();
        this.setStage(Stage.ENUMERATING);
        this.dispatch(ConnectionStageChangeEvent.Stage.CONNECTING);
    }

    protected void onDeviceEnumerated() {
        this.debug("onDeviceEnumerated() [EV]");
        try {
            this.obtainEndpoints();
            this.subscribeToEndpoints();
        }
        catch(UnsupportedOperationException ex) {
            this.debug("- The device is not supported: %1$s", ex.getMessage());
            this.gracefullyClose(true, false);
            return;
        }
        this.setStage(Stage.SUBSCRIBING);
        this.dispatch(ConnectionStageChangeEvent.Stage.CONNECTING);
    }

    private void obtainEndpoints() throws UnsupportedOperationException {
        this.debug("- obtainEndpoints()");
        BluetoothGattService service = this.getGatt().getService(BLEOptions.Device.SERVICE);
        if(service == null) throw new UnsupportedOperationException("No such service");
        BluetoothGattCharacteristic read  = service.getCharacteristic(BLEOptions.Device.Serial.Read.ENDPOINT);
        BluetoothGattCharacteristic write = service.getCharacteristic(BLEOptions.Device.Serial.Write.ENDPOINT);
        if(read == null || write == null) throw new UnsupportedOperationException("No read/write endpoints");
        if((read.getProperties() & BLEOptions.Device.Serial.Read.PROPERTY) == 0) throw new UnsupportedOperationException("Read endpoint property mismatch");
        if((write.getProperties() & BLEOptions.Device.Serial.Write.PROPERTY) == 0) throw new UnsupportedOperationException("Write endpoint property mismatch");
        this.endpoints = new CharacteristicStore(service, read, write);
    }

    private void subscribeToEndpoints() throws UnsupportedOperationException {
        this.debug("- subscribeToEndpoints()");
        BluetoothGattDescriptor descriptor = this.getEndpoints().read.getDescriptor(BLEOptions.Device.Serial.Read.DESCRIPTOR);
        if(descriptor == null) throw new UnsupportedOperationException("No CCCD descriptor");
        if(!this.getGatt().setCharacteristicNotification(this.getEndpoints().read, true)) throw new UnsupportedOperationException("Failed to enable notifications locally");
        descriptor.setValue(BLEOptions.Device.Serial.Read.DESCRIPTOR_VAL);
        this.getGatt().writeDescriptor(descriptor);
    }

    protected void onDescriptorWritten(BluetoothGattDescriptor descriptor) {
        this.debug("onDescriptorWritten() [EV]");
        if(     descriptor == null
            || !descriptor.getCharacteristic().getUuid()
                    .equals(this.getEndpoints().read.getUuid())
            || !descriptor.getUuid()
                    .equals(BLEOptions.Device.Serial.Read.DESCRIPTOR))
            return;
        this.onNotificationSubscribed();
    }

    private void onNotificationSubscribed() {
        this.debug("- onNotificationSubscribed()");
        this.setDelayedTask(this::onConnectionStabilized, BLEOptions.Connection.CONNECTION_GRACE_PERIOD);
        this.setStage(Stage.CONNECTION_GRACE_PERIOD);
        this.dispatch(ConnectionStageChangeEvent.Stage.CONNECTING);
    }

    private void onConnectionStabilized() {
        this.debug("onConnectionStabilized() [EV]");
        this.setStage(Stage.ESTABLISHED);
        this.dispatch(ConnectionStageChangeEvent.Stage.ESTABLISHED);
    }

    protected void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        this.debug("onCharacteristicChanged() [EV]");
        if(!this.ensureStage(Stage.ESTABLISHED)) {
            this.debug("- Connection flow still not done, we are at stage %1$s", this.internalStage);
            return;
        }
        if(characteristic.getUuid().equals(Objects.requireNonNull(this.getEndpoints().read).getUuid())) {
            this.onDataReceived();
        }
    }

    private void onDataReceived() {
        this.debug("- onDataReceived()");
        // TODO: data handling
    }

    @Override
    public boolean write(@NonNull byte[] data) {
        throw new UnsupportedOperationException("stub!");
    }

    protected void onDeviceDisconnected() {
        this.debug("onDeviceDisconnected() [EV]");
        this.gracefullyClose(false, false);
    }

    protected void onBluetoothError() {
        this.debug("onBluetoothError() [EV]");
        this.gracefullyClose(true, true);
    }

    private void gracefullyClose(boolean failed, boolean retryAdvised) {
        this.debug(">> Gracefully closing, failed: %1$s, retry advised: %2$s", failed, retryAdvised);
        this.cancelDelayedTask();
        if(this.gattObj != null) {
            this.getGatt().close();
        }
        else {
            failed = true;
            retryAdvised = true;
            this.debug(">> Gatt object is null.");
        }
        this.setStage(failed ? Stage.CRASHED : Stage.DISCONNECTED);
        this.wipeReferences();
        this.dispatch(!failed ? ConnectionStageChangeEvent.Stage.DISCONNECTED : retryAdvised ? ConnectionStageChangeEvent.Stage.FAILED_RETRY : ConnectionStageChangeEvent.Stage.FAILED_NO_RETRY);
    }

}


enum Stage {
    DISCONNECTED(100),
    CONNECTING(0),
    REQUESTING_MTU(30),
    ENUMERATING(50),
    SUBSCRIBING(70),
    CONNECTION_GRACE_PERIOD(90),
    ESTABLISHED(100),
    DISCONNECTING(0),
    CRASHED(100),
    ;

    private final int percentage;

    Stage(int percentage) {
        this.percentage = percentage;
    }

    public int getPercentage() {
        return this.percentage;
    }

}


class CharacteristicStore {

    @NonNull
    public final BluetoothGattService service;

    @NonNull
    public final BluetoothGattCharacteristic read;

    @NonNull
    public final BluetoothGattCharacteristic write;

    public CharacteristicStore(@NonNull BluetoothGattService service, @NonNull BluetoothGattCharacteristic read, @NonNull BluetoothGattCharacteristic write) {
        this.service = service;
        this.read = read;
        this.write = write;
    }

}


class DelayedTask implements Runnable {

    @NonNull
    private final Runnable core;

    private boolean isCancelled = false;

    public DelayedTask(@NonNull Runnable core) {
        this.core = core;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    @Override
    public void run() {
        if(this.isCancelled) return;
        this.core.run();
        this.isCancelled = true;
    }

}


class BluetoothGattCallbackImpl extends BluetoothGattCallback {

    @NonNull
    private final AndroidBLESensorConnection connection;

    public BluetoothGattCallbackImpl(@NonNull AndroidBLESensorConnection connection) {
        this.connection = connection;
    }

    private void post(@NonNull Runnable r) {
        this.connection.postTask(r);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            switch(newState) {
                case BluetoothGatt.STATE_CONNECTED: {
                    this.post(() -> this.connection.onDeviceConnected(gatt));
                    break;
                }
                case BluetoothGatt.STATE_DISCONNECTED: {
                    this.post(this.connection::onDeviceDisconnected);
                    break;
                }
            }
        }
        else {
            this.post(this.connection::onBluetoothError);
        }
    }

    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.post(() -> this.connection.onDeviceMTURequested(mtu));
        } else {
            this.post(this.connection::onBluetoothError);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            this.post(this.connection::onDeviceEnumerated);
        }
        else {
            this.post(this.connection::onBluetoothError);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            this.post(() -> this.connection.onDescriptorWritten(descriptor));
        }
        else {
            this.post(this.connection::onBluetoothError);
        }
    }

    @Override
    public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        this.post(() -> this.connection.onCharacteristicChanged(characteristic));
    }

}


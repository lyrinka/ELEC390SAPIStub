package app.uvtracker.sensor.pii.connection.application;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Objects;

import app.uvtracker.data.type.OpticalRecord;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewEstimationReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.packet.ISensorPacketConnection;
import app.uvtracker.sensor.pii.connection.packet.event.ParsedPacketReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.event.IEventSource;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalEstimation;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalSample;
import app.uvtracker.sensor.protocol.packet.in.PacketInSyncData;
import app.uvtracker.sensor.protocol.packet.in.PacketInSyncInfo;
import app.uvtracker.sensor.protocol.packet.out.PacketOutRequestSyncData;
import app.uvtracker.sensor.protocol.packet.out.PacketOutRequestSyncInfo;

public class PIISensorConnectionImpl extends EventRegistry implements ISensorConnection, IEventListener {

    @NonNull
    private final ISensorPacketConnection baseConnection;

    @NonNull
    private final EventRegistry packetEventRegistry;

    @NonNull
    private final SyncManager syncManager;

    public PIISensorConnectionImpl(@NonNull ISensorPacketConnection baseConnection) {
        this.packetEventRegistry = new EventRegistry();
        this.baseConnection = baseConnection;
        this.syncManager = new SyncManager(this);
        this.baseConnection.registerListener(this);
        this.packetEventRegistry.registerListener(this);
    }

    @NonNull
    protected ISensorPacketConnection getBaseConnection() {
        return this.baseConnection;
    }

    // Base connection implementation
    @Override
    @NonNull
    public ISensor getSensor() {
        return this.baseConnection.getSensor();
    }

    @Override
    public void reset() {
        this.baseConnection.reset();
    }

    @Override
    public boolean connect() {
        return this.baseConnection.connect();
    }

    @Override
    public boolean disconnect() {
        return this.baseConnection.disconnect();
    }

    public boolean sync() {
        return this.syncManager.startSync();
    }

    // TODO: any better way to propagate these events up?
    @EventHandler
    protected void onConnectionStateChange(@NonNull ConnectionStateChangeEvent event) {
        this.dispatch(event);
    }

    // Packet event registry
    @EventHandler
    protected void onParsedPacketReception(@NonNull ParsedPacketReceivedEvent event) {
        this.packetEventRegistry.dispatch(event.getPacket());
    }

    @NonNull
    public IEventSource getPacketEventManager() {
        return this.packetEventRegistry;
    }

    // Packet handling
    @EventHandler
    protected void onPacketInNewOpticalSample(PacketInNewOpticalSample packet) {
        this.dispatch(new NewSampleReceivedEvent(packet));
    }

    @EventHandler
    protected void onPacketInNewOpticalEstimation(PacketInNewOpticalEstimation packet) {
        this.dispatch(new NewEstimationReceivedEvent(packet));
    }

}

class SyncManager implements IEventListener {

    private static final String TAG = SyncManager.class.getSimpleName();

    enum Stage {
        DISCONNECTED,
        IDLE,
        INITIATED,
        PROCESSING,
    }

    @NonNull
    private final PIISensorConnectionImpl connection;

    private Stage stage;

    @NonNull
    private final TimeoutTask timeoutTask;

    @NonNull
    private final HashMap<Integer, OpticalRecord> volatileStorage;

    private int volatileStorageStart = -1;
    private int volatileStorageEnd = -1;

    @Nullable PacketInSyncInfo latestSyncInfo;

    public SyncManager(@NonNull PIISensorConnectionImpl connection) {
        this.connection = connection;
        this.stage = Stage.IDLE;
        this.timeoutTask = new TimeoutTask(1000, () -> {
            Log.d(TAG, "Timeout.");
            this.stage = Stage.IDLE;
        });
        this.volatileStorage = new HashMap<>();
        this.connection.registerListener(this);
        this.connection.getPacketEventManager().registerListener(this);
    }

    @EventHandler
    private void onConnectionStateChange(ConnectionStateChangeEvent event) {
        Log.d(TAG, "onConnectionStateChange() [EV]");
        switch(event.getStage()) {
            case ESTABLISHED:
                Log.d(TAG, "Connection established. Stage change to IDLE");
                this.stage = Stage.IDLE;
                break;
            case DISCONNECTED:
            case FAILED_RETRY:
            case FAILED_NO_RETRY:
                Log.d(TAG, "Connection lost. Stage change to DISCONNECTED");
                this.stage = Stage.DISCONNECTED;
                break;
        }
    }

    public boolean startSync() {
        Log.d(TAG, "startSync()");
        if(this.stage != Stage.IDLE) return false;
        if(!this.connection.getBaseConnection().write(new PacketOutRequestSyncInfo())) return false;
        Log.d(TAG, "Sync info request packet sent.");
        this.stage = Stage.INITIATED;
        this.timeoutTask.reset();
        return true;
    }

    @EventHandler
    private void onSyncInfoReceived(PacketInSyncInfo packet) {
        Log.d(TAG, "onSyncInfoReceived() [EV]");
        if(this.stage != Stage.INITIATED) return;
        if(packet.getSampleCount() == 0) {
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            return;
        }
        Log.d(TAG, "Processing sync info packet...");
        this.latestSyncInfo = packet;
        this.stage = Stage.PROCESSING;
        this.timeoutTask.refresh();
        this.processSync();
    }

    private void processSync() {
        Log.d(TAG, "processSync()");
        Objects.requireNonNull(this.latestSyncInfo);
        int first = this.latestSyncInfo.getSampleStart();
        int last = first + this.latestSyncInfo.getSampleCount() - 1;
        Log.d(TAG, "Remote DB interval: " + first + ", " + last);
        // Find Interval{SyncInfo} - Interval{VolatileStorage}
        Log.d(TAG, "Local DB interval: " + this.volatileStorageStart + ", " + this.volatileStorageEnd);
        if(this.volatileStorageStart < 0 || this.volatileStorageEnd < 0) {
            // Abide to sync info
        }
        else if(last > this.volatileStorageEnd) {
            first = volatileStorageEnd + 1;
        }
        else if(first < this.volatileStorageStart) {
            last = this.volatileStorageStart - 1;
        }
        else {
            Log.d(TAG, "Nothing to synchronize. Aborting..");
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            return;
        }
        if(last - first + 1 > PacketOutRequestSyncData.MAX_COUNT)
            first = last - PacketOutRequestSyncData.MAX_COUNT + 1;
        Log.d(TAG, "Requesting remote DB: " + first + ", " + last);
        this.connection.getBaseConnection().write(new PacketOutRequestSyncData(first, last - first + 1));
    }

    @EventHandler
    private void onSyncDataReceived(PacketInSyncData packet) {
        Log.d(TAG, "onSyncDataReceived() [EV]");
        if(this.stage != Stage.PROCESSING) return;
        this.timeoutTask.refresh();
        int first = packet.getStartSample();
        int last = first + packet.getRecords().length - 1;
        Log.d(TAG, "Received remote DB data: " + first + ", " + last);
        if(this.volatileStorageStart < 0 || this.volatileStorageEnd < 0) {
            this.volatileStorageStart = first;
            this.volatileStorageEnd= last;
        }
        else {
            if (first < this.volatileStorageStart) this.volatileStorageStart = first;
            if (last > this.volatileStorageEnd) this.volatileStorageEnd = last;
        }
        for(OpticalRecord record : packet.getRecords()) {
            this.volatileStorage.put(first++, record);
        }
        this.processSync();
    }

}

class TimeoutTask {

    private final String TAG = TimeoutTask.class.getSimpleName();

    @NonNull
    private final Handler handler;

    private boolean pending;

    private int session;

    private final int timeout;

    @NonNull
    private final Runnable callback;

    public TimeoutTask(int timeout, @NonNull Runnable callback) {
        if(timeout == 0) throw new IllegalArgumentException();
        this.handler = new Handler(Looper.getMainLooper());
        this.pending = true;
        this.session = 0;
        this.timeout = timeout;
        this.callback = callback;
    }

    public boolean isPending() {
        return this.pending;
    }

    public synchronized void reset() {
        Log.d(TAG, "Reset:");
        this.pending = true;
        this.refresh();
    }

    public synchronized boolean refresh() {
        if(!this.pending) return false;
        if(this.timeout <= 0) return false;
        this.session++;
        int sessionCapture = this.session;
        this.handler.postDelayed(() -> this.execute(sessionCapture), this.timeout);
        Log.d(TAG, "Refreshed. Session " + this.session);
        return true;
    }

    public synchronized void cancel() {
        this.pending = false;
        this.session++;
        Log.d(TAG, "Cancelled. Session " + this.session);
    }

    private synchronized void execute(int session) {
        Log.d(TAG, "Executing..");
        synchronized(this) {
            if (!this.pending) return;
            if (this.session > session) return;
            this.pending = false;
        }
        Log.d(TAG, "Invoking callback..");
        this.callback.run();
    }

}

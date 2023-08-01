package app.uvtracker.sensor.pii.connection.application;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.uvtracker.data.optical.cache.IOpticalDataCache;
import app.uvtracker.data.optical.cache.IOpticalDataCacheReader;
import app.uvtracker.sensor.BLEOptions;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewEstimationReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncProgressEvent;
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

    @Override
    @NonNull
    public IOpticalDataCacheReader getOpticalDataCacheReader() {
        return this.syncManager.getCache();
    }

    @Override
    public boolean isSyncing() {
        return this.syncManager.isSyncing();
    }

    @Override
    public boolean startSync() {
        return this.syncManager.startSync();
    }

    @Override
    public boolean abortSync() {
        return this.syncManager.abortSync();
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
        INITIATING,
        PROCESSING,
    }

    @NonNull
    private final PIISensorConnectionImpl connection;

    private Stage stage;

    @NonNull
    private final TimeoutTask timeoutTask;

    @Nullable
    private PacketInSyncInfo latestSyncInfo;

    @NonNull
    private final IOpticalDataCache cache;

    private int progressInfoCount;
    private int progressInfoTotalCount;

    public SyncManager(@NonNull PIISensorConnectionImpl connection) {
        this.connection = connection;
        this.stage = Stage.DISCONNECTED;
        this.timeoutTask = new TimeoutTask(BLEOptions.Sync.SYNC_TIMEOUT, this::abortSync);
        this.cache = IOpticalDataCache.getVolatileImpl();
        this.connection.registerListener(this);
        this.connection.getPacketEventManager().registerListener(this);
    }

    @NonNull
    public IOpticalDataCache getCache() {
        return this.cache;
    }

    public boolean isSyncing() {
        return this.stage == Stage.INITIATING || this.stage == Stage.PROCESSING;
    }

    public boolean startSync() {
        Log.d(TAG, "startSync()");
        if(this.stage != Stage.IDLE) return false;
        if(!this.connection.getBaseConnection().write(new PacketOutRequestSyncInfo())) return false;
        Log.d(TAG, "Sync info request packet sent.");
        this.stage = Stage.INITIATING;
        this.timeoutTask.refresh();
        this.connection.dispatch(new SyncProgressEvent(SyncProgressEvent.Stage.INITIATING));
        return true;
    }

    public boolean abortSync() {
        Log.d(TAG, "abortSync()");
        if(this.stage == Stage.INITIATING || this.stage == Stage.PROCESSING) {
            this.stage = Stage.IDLE;
            this.connection.dispatch(new SyncProgressEvent(SyncProgressEvent.Stage.ABORTED));
            return true;
        }
        return false;
    }

    @EventHandler
    private void onSyncInfoReceived(PacketInSyncInfo packet) {
        Log.d(TAG, "onSyncInfoReceived() [EV]");
        if(this.stage != Stage.INITIATING) return;
        this.stage = Stage.PROCESSING;
        this.timeoutTask.refresh();
        Log.d(TAG, "Processing sync info packet...");
        this.latestSyncInfo = packet;
        this.connection.dispatch(new SyncProgressEvent(SyncProgressEvent.Stage.INITIATING));
        this.progressInfoCount = 0;
        this.processSync(true);
    }

    @EventHandler
    private void onSyncDataReceived(PacketInSyncData packet) {
        Log.d(TAG, "onSyncDataReceived() [EV]");
        if(this.stage != Stage.PROCESSING) return;
        this.timeoutTask.refresh();
        int start = packet.getStartSample();
        int count = packet.getRecords().length;
        Log.d(TAG, "Received remote DB data: " + start + ", " + count);
        this.cache.writeInterval(start, packet.getRecords());
        this.progressInfoCount += count;
        this.connection.dispatch(new SyncProgressEvent(SyncProgressEvent.Stage.PROCESSING, Math.round((float)this.progressInfoCount / (float)this.progressInfoTotalCount * 100.0f)));
        this.processSync(false);
    }

    private void processSync(boolean firstTime) {
        Log.d(TAG, "processSync()");
        Objects.requireNonNull(this.latestSyncInfo);
        if(this.latestSyncInfo.getSampleCount() == 0) {
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            this.connection.dispatch(new SyncProgressEvent(SyncProgressEvent.Stage.DONE_NOUPDATE));
            return;
        }
        int[] temp = this.cache.computeRequestInterval(this.latestSyncInfo.getSampleStart(), this.latestSyncInfo.getSampleStart() + this.latestSyncInfo.getSampleCount() - 1);
        if(temp == null) {
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            this.connection.dispatch(new SyncProgressEvent(firstTime ? SyncProgressEvent.Stage.DONE_NOUPDATE : SyncProgressEvent.Stage.DONE));
            return;
        }
        int first = temp[0];
        int last = temp[1];
        if(firstTime) {
            this.progressInfoTotalCount = last - first + 1;
        }
        int count = Integer.min(last - first + 1, PacketOutRequestSyncData.MAX_COUNT);
        first = last - count + 1;
        Log.d(TAG, "Requesting remote DB: " + first + ", " + count);
        this.connection.getBaseConnection().write(new PacketOutRequestSyncData(first, count));
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
                boolean flag = this.isSyncing();
                this.stage = Stage.DISCONNECTED;
                if(flag) this.connection.dispatch(new SyncProgressEvent(SyncProgressEvent.Stage.ABORTED));
                break;
        }
    }

}

class TimeoutTask {

    @NonNull
    private final Handler handler;

    private int session;

    private final int timeout;

    @NonNull
    private final Runnable callback;

    public TimeoutTask(int timeout, @NonNull Runnable callback) {
        if(timeout == 0) throw new IllegalArgumentException();
        this.handler = new Handler(Looper.getMainLooper());
        this.session = 0;
        this.timeout = timeout;
        this.callback = callback;
    }

    public synchronized void refresh() {
        if(this.timeout <= 0) return;
        this.session++;
        int sessionCapture = this.session;
        this.handler.postDelayed(() -> this.execute(sessionCapture), this.timeout);
    }

    public synchronized void cancel() {
        this.session++;
    }

    private synchronized void execute(int session) {
        synchronized(this) {
            if (this.session > session) return;
        }
        this.callback.run();
    }

}

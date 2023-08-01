package app.uvtracker.sensor.pii.connection.application;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.cache.IOpticalDataCache;
import app.uvtracker.sensor.pii.connection.shared.IConnectable;

public interface ISensorConnection extends IConnectable {

    /* -------- RT data notifications -------- */

    // Emits event: NewSampleReceivedEvent, NewEstimationReceivedEvent


    /* -------- Sync features -------- */

    // Emits event: SyncProgressEvent

    @NonNull
    IOpticalDataCache getOpticalDataCache();

    boolean isSyncing();

    boolean startSync();

    boolean abortSync();


    // TODO: more features

}

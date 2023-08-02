package app.uvtracker.sensor.pii.connection.application;

import app.uvtracker.sensor.pii.connection.shared.IConnectable;

public interface ISensorConnection extends IConnectable {

    /* -------- RT data notifications -------- */

    // Emits event: NewSampleReceivedEvent, NewEstimationReceivedEvent


    /* -------- Sync features -------- */

    // Emits event: SyncProgressEvent, SyncDataReceivedEvent

    boolean isSyncing();

    boolean startSync();

    boolean forceSync();

    boolean abortSync();


    // TODO: more features

}

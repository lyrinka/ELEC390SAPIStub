package app.uvtracker.sensor.pii.connection;

import androidx.annotation.NonNull;

public interface ISensorConnection extends IConnectable {

    boolean write(@NonNull byte[] data);

}

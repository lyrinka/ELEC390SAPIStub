package app.uvtracker.sensor.api.type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface AndroidBLESensor {

    @NonNull
    String getAddress();

    @Nullable
    String getName();

    boolean isConnected();

    void connect();

}

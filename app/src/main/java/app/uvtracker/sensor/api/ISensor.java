package app.uvtracker.sensor.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ISensor {

    @NonNull
    String getAddress();

    @Nullable
    String getName();

    boolean isConnected();

    void connect();

}

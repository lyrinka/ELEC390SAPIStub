package app.uvtracker.sensor.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Consumer;

public interface ISensor {

    enum ConnectionStatus {
        ESTABLISHED,
        DISCONNECTED,
        FAILED_RETRY,
        FAILED_NO_RETRY,
    }

    @NonNull
    String getAddress();

    @Nullable
    String getName();

    // Connection flow
    boolean isConnected();

    boolean connect();

    boolean disconnect();

    void forceReset();

    boolean registerConnectionStatusCallback(@NonNull Consumer<ConnectionStatus> callback);

    boolean unregisterConnectionStatusCallback(@NonNull Consumer<ConnectionStatus> callback);

    // Features: WIP

}

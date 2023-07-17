package app.uvtracker.sensor.pii.connection;

import androidx.annotation.NonNull;

public class ConnectionStageChangeEvent {

    public enum Stage {
        CONNECTING,
        ESTABLISHED,
        DISCONNECTING,
        DISCONNECTED,
        FAILED_RETRY,
        FAILED_NO_RETRY,
    }

    @NonNull
    private final Stage stage;

    private final int percentage;

    public ConnectionStageChangeEvent(@NonNull Stage stage, int estimatedPercentage) {
        this.stage = stage;
        this.percentage = estimatedPercentage;
    }

    @NonNull
    public Stage getStage() {
        return this.stage;
    }

    public int getPercentage() {
        return this.percentage;
    }

}

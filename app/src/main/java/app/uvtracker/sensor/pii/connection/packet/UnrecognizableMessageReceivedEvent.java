package app.uvtracker.sensor.pii.connection.packet;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

public class UnrecognizableMessageReceivedEvent {

    @NonNull
    private final String message;

    public UnrecognizableMessageReceivedEvent(@NonNull String message) {
        this.message = message;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public byte[] getMessageAsBytes() {
        return message.getBytes(StandardCharsets.US_ASCII);
    }

    @NonNull
    public String getMessageAsUnicode() {
        return new String(message.trim().getBytes(StandardCharsets.US_ASCII), StandardCharsets.UTF_8);
    }

}

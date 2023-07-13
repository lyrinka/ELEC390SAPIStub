package app.uvtracker.sensor.protocol.packet;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;

public class PacketInDebug extends PacketIn {

    @NonNull
    private final String message;

    public PacketInDebug(Packet basePacket) {
        super(basePacket);
        this.message = new String(this.payload, Charset.defaultCharset());
    }

    @NonNull
    public String getMessage() {
        return this.message;
    }

    @Override
    @NonNull
    public String toString() {
        return this.type + "{\"" + this.getMessage() + "\"}";
    }

}

package app.uvtracker.sensor.pii.connection.application;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pii.connection.packet.ISensorPacketConnection;
import app.uvtracker.sensor.pii.connection.packet.ParsedPacketReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.event.IEventSource;

public class SensorConnection extends EventRegistry implements ISensorConnection, IEventListener {

    @NonNull
    private final ISensorPacketConnection baseConnection;

    @NonNull
    private final EventRegistry packetEventRegistry;

    public SensorConnection(@NonNull ISensorPacketConnection baseConnection) {
        this.packetEventRegistry = new EventRegistry();
        this.baseConnection = baseConnection;
        this.registerListener(this);
    }

    // Base connection implementation
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

    // TODO: any better way to propagate these events up?
    @EventHandler
    protected void onConnectionStateChange(@NonNull ConnectionStateChangeEvent event) {
        this.dispatch(event);
    }

    // Specific implementation
    @EventHandler
    protected void onParsedPacketReception(@NonNull ParsedPacketReceivedEvent event) {
        this.packetEventRegistry.dispatch(event.getPacket());
    }

    @NonNull
    public IEventSource getPacketEventManager() {
        return this.packetEventRegistry;
    }


}

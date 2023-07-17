package app.uvtracker.sensor.api.event;

public interface IEventSource {

    void register(IEventListener listener);

    default void unregisterAll() {
        throw new UnsupportedOperationException("Unregistering event handlers is not supported.");
    }

}

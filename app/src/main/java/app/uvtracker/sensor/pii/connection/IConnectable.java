package app.uvtracker.sensor.pii.connection;

import app.uvtracker.sensor.api.event.IEventSource;

public interface IConnectable extends IEventSource {

    void reset();

    boolean connect();

    boolean disconnect();

}

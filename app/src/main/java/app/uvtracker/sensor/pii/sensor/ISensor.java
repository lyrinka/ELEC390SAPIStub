package app.uvtracker.sensor.pii.sensor;

import app.uvtracker.sensor.pii.connection.IConnectable;

public interface ISensor extends IConnectable {

    void reset();

    boolean connect();

    boolean disconnect();

}

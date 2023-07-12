package app.uvtracker.sensor.api.exception.comms;

public class ConnectionInactiveException extends CommunicationException {

    public ConnectionInactiveException() {
    }

    public ConnectionInactiveException(String message) {
        super(message);
    }

}

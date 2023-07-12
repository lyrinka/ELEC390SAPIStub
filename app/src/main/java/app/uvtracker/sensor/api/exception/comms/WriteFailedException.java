package app.uvtracker.sensor.api.exception.comms;

public class WriteFailedException extends CommunicationException {

    public WriteFailedException() {
    }

    public WriteFailedException(String message) {
        super(message);
    }

}

package app.uvtracker.sensor.protocol.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;

public class PacketInButtonInteract extends PacketIn {

    public enum Button {
        FIRST,
        SECOND,
    }

    public enum Action {
        PRESSED,
        RELEASED,
    }

    @NonNull
    private final Button button;

    @NonNull
    private final Action action;

    public PacketInButtonInteract(Packet packetBase) throws PacketFormatException {
        super(packetBase);
        if(this.payload.length != 1)
            throw new PacketFormatException("Expected 1 byte.", packetBase);
        byte data = this.payload[0];
        int buttonCode = data & 0x0F;
        int actionCode = (data & 0xF0) >> 4;
        switch(buttonCode) {
            case 0: this.button = Button.FIRST; break;
            case 1: this.button = Button.SECOND; break;
            default: throw new PacketFormatException("Undefined button number.", packetBase);
        }
        switch(actionCode) {
            case 0: this.action = Action.RELEASED; break;
            case 1: this.action = Action.PRESSED; break;
            default: throw new PacketFormatException("Undefined action code.", packetBase);
        }
    }

    @NonNull
    public Button getButton() {
        return this.button;
    }

    @NonNull
    public Action getAction() {
        return this.action;
    }

    @Override
    @NonNull
    public String toString() {
        return this.type + "{" + this.button.toString().toLowerCase() + " button " + this.action.toString().toLowerCase() + "}";
    }

}

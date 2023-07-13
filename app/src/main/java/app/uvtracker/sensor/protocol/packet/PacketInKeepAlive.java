package app.uvtracker.sensor.protocol.packet;

import androidx.annotation.NonNull;

public class PacketInKeepAlive extends PacketIn {

    private final int keepAliveID;

    public PacketInKeepAlive(Packet basePacket) {
        super(basePacket);
        if(this.payload.length == 4) {
            this.keepAliveID =
                       this.payload[0]
                    | (this.payload[1] << 8)
                    | (this.payload[2] << 16)
                    | (this.payload[3] << 24);
        }
        else {
            this.keepAliveID = -1;
        }
    }

    public int getKeepAliveID() {
        return this.keepAliveID;
    }

    @Override
    @NonNull
    public String toString() {
        if(this.keepAliveID < 0)
            return this.type + "{}";
        else
            return this.type + "{" + this.keepAliveID + "}";
    }

}

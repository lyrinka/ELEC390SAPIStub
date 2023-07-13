package app.uvtracker.sensor.protocol.packet;

public class PacketOutKeepAlive extends PacketOut {

    public PacketOutKeepAlive() {
        super(PacketType.OUT.KEEP_ALIVE);
    }

    public PacketOutKeepAlive(int keepAliveID) {
        super(PacketType.OUT.KEEP_ALIVE, 4);
        this.setKeepAliveID(keepAliveID);
    }

    public void setKeepAliveID(int keepAliveID) {
        if(this.payload.length != 4)
            this.payload = new byte[4];
        this.payload[0] = (byte)(keepAliveID & 0xFF);
        this.payload[1] = (byte)((keepAliveID >>  8) & 0xFF);
        this.payload[2] = (byte)((keepAliveID >> 16) & 0xFF);
        this.payload[3] = (byte)((keepAliveID >> 24) & 0xFF);
    }

}

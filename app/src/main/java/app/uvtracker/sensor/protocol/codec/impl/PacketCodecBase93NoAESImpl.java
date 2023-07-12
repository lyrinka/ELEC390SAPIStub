package app.uvtracker.sensor.protocol.codec.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.uvtracker.sensor.protocol.codec.IPacketCodec;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.codec.exception.MTUExceededException;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.codec.helper.Base93Helper;
import app.uvtracker.sensor.protocol.codec.helper.CRC8Helper;
import app.uvtracker.sensor.protocol.packet.Packet;
import app.uvtracker.sensor.protocol.type.PacketDirection;
import app.uvtracker.sensor.protocol.type.PacketType;

public class PacketCodecBase93NoAESImpl implements IPacketCodec {

    private static PacketCodecBase93NoAESImpl instance;

    public static PacketCodecBase93NoAESImpl getInstance() {
        if(PacketCodecBase93NoAESImpl.instance == null)
            PacketCodecBase93NoAESImpl.instance = new PacketCodecBase93NoAESImpl();
        return PacketCodecBase93NoAESImpl.instance;
    }

    public static final int MTU = 122; // 128 - 3 (BLE) - 3 (FSM)

    @NonNull
    @Override
    public String encode(@NonNull Packet packet) throws CodecException {
        byte[] serialized = this.serialize(packet);
        String encoded = Base93Helper.encode(serialized);
        if(encoded.length() > MTU) throw new MTUExceededException("Device MTU exceeded.");
        return encoded;
    }

    @NonNull
    private byte[] serialize(@NonNull Packet packet) throws CodecException {
        int length = packet.getPayload().length;
        if(length > 255) throw new MTUExceededException("Protocol MTU exceeded.");
        byte[] data = new byte[length + 3];
        data[0] = this.serializeHeader(packet.getType());
        data[1] = (byte)length;
        System.arraycopy(packet.getPayload(), 0, data, 2, packet.getPayload().length);
        data[length + 2] = CRC8Helper.compute(data, length + 2);
        return data;
    }

    private byte serializeHeader(@NonNull PacketType type) {
        if(type.getID() > 127 || type.getID() < 0) throw new IllegalArgumentException("PacketID " + type.getID() + " out of range. Check code for bugs.");
        byte header = (byte)type.getID();
        if(type.getDirection() == PacketDirection.IN) header |= 0x80;
        return header;
    }


    @NonNull
    @Override
    public Packet decode(@NonNull String packet) throws CodecException {
        return this.deserialize(Base93Helper.decode(packet));
    }

    @NonNull
    private Packet deserialize(@NonNull byte[] data) throws CodecException {
        if(data.length < 3) throw new PacketFormatException("Packet is too small.");
        PacketType type = this.deserializeHeader(data[0]);
        if(type == null) throw new PacketFormatException("Packet ID " + (data[0] & 0x7F) + " does not exist.");
        int length = data[1];
        if(data.length < length + 3) throw new PacketFormatException("Packet is too small and does not contain necessary fields.");
        byte crc = CRC8Helper.compute(data, length + 2);
        if(crc != data[length + 2]) throw new PacketFormatException("Packet is corrupted.");
        byte[] payload = new byte[length];
        System.arraycopy(data, 2, payload, 0, length);
        return new Packet(type, payload);
    }

    @Nullable
    private PacketType deserializeHeader(byte header) {
        if((header & 0x80) == 0) { // OUT
            return PacketType.OUT.getByID(header & 0x7F);
        }
        else { // IN
            return PacketType.IN.getByID(header & 0x7F);
        }
    }

}

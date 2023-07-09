package com.example.elec390.sapi.protocol.codec.impl;


import androidx.core.graphics.PaintKt;

import com.example.elec390.sapi.protocol.PacketBase;
import com.example.elec390.sapi.protocol.PacketDirection;
import com.example.elec390.sapi.protocol.PacketType;
import com.example.elec390.sapi.protocol.codec.IPacketCodec;
import com.example.elec390.sapi.protocol.codec.exception.CodecException;
import com.example.elec390.sapi.protocol.codec.exception.PacketFormatException;

public class PacketCodecBase93Impl implements IPacketCodec {

    public static int MTU = 93;

    @Override
    public PacketBase decode(String symbolStream) throws CodecException {
        byte[] byteStream = Base93Helper.getInstance().base93Decode(symbolStream);
        if(byteStream.length < 2) throw new PacketFormatException("Packet does not contain a header.");
        PacketType packetType = new PacketType((byteStream[0] & 0x80) != 0 ? PacketDirection.IN : PacketDirection.OUT, (byte)(byteStream[0] & 0x7F));
        int payloadLength = byteStream[1];
        if(byteStream.length < 3 + payloadLength) throw new PacketFormatException("Packet is shorter than expected.");
        byte[] payload = new byte[payloadLength];
        System.arraycopy(byteStream, 2, payload, 0, payloadLength);
        byte crc = byteStream[payloadLength + 2];
        if(!CRC8Helper.getInstance().validateCRC(byteStream, 0, byteStream.length - 1, crc)) throw new PacketFormatException("Packet is corrupted.");
        return new PacketBase(packetType, payload);
    }

    @Override
    public String encode(PacketBase packet) throws CodecException {
        int payloadLength = packet.getPayload().length;
        if(payloadLength > MTU) throw new PacketFormatException("Outbound packet will exceed MTU.");
        int packetHeader = packet.getPacketType().getPacketID() & 0x7F;
        if(packet.getPacketType().getPacketDirection() == PacketDirection.IN) packetHeader |= 0x80;
        byte[] byteStream = new byte[payloadLength + 3];
        byteStream[0] = (byte)packetHeader;
        byteStream[1] = (byte)payloadLength;
        System.arraycopy(packet.getPayload(), 0, byteStream, 2, payloadLength);
        byte crc = CRC8Helper.getInstance().computeCRC(byteStream, 0, payloadLength + 2);
        byteStream[payloadLength + 2] = crc;
        return Base93Helper.getInstance().base93Encode(byteStream);
    }

}

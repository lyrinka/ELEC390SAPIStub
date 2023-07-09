package com.example.elec390.sapi.protocol.codec;

import com.example.elec390.sapi.protocol.PacketBase;
import com.example.elec390.sapi.protocol.codec.exception.CodecException;

public interface IPacketCodec {

    PacketBase decode(String symbolstream) throws CodecException;
    String encode(PacketBase packet) throws CodecException;

}

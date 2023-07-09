package com.example.elec390.sapi.protocol.codec;

import com.example.elec390.sapi.protocol.codec.impl.PacketCodecBase93Impl;

public class PacketCodecManager {

    private static PacketCodecManager instance;

    public static PacketCodecManager getInstance() {
        if(PacketCodecManager.instance == null)
            PacketCodecManager.instance = new PacketCodecManager();
        return PacketCodecManager.instance;
    }

    private IPacketCodec codecInstance;

    private PacketCodecManager() {
        this.codecInstance = new PacketCodecBase93Impl();
    }

    public IPacketCodec getCodec() {
        return this.codecInstance;
    }

}

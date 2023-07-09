package com.example.elec390.sapi.protocol.codec.util;

public class CRC8Helper {

    private static CRC8Helper instance;

    public static CRC8Helper getInstance() {
        if(CRC8Helper.instance == null)
            CRC8Helper.instance = new CRC8Helper();
        return CRC8Helper.instance;
    }

    public byte computeCRC(byte[] input) {
        return this.computeCRC(input, 0, input.length);
    }

    public byte computeCRC(byte[] input, int index, int length) {
        // TODO: actual CRC impl
        return (byte) 0xAB;
    }

    public boolean validateCRC(byte[] input, byte crc) {
        return this.validateCRC(input, 0, input.length, crc);
    }

    public boolean validateCRC(byte[] input, int index, int length, byte crc) {
        // TODO: actual CRC impl
        return true;
    }

}

package com.example.elec390.sapi.protocol.codec.util;

import com.example.elec390.sapi.protocol.codec.exception.Base93UnalignedException;
import com.example.elec390.sapi.protocol.codec.exception.CodecException;
import com.example.elec390.sapi.protocol.codec.exception.MTUExceededException;

public class Base93Helper {

    private static Base93Helper instance;

    public static Base93Helper getInstance() {
        if(Base93Helper.instance == null)
            Base93Helper.instance = new Base93Helper();
        return Base93Helper.instance;
    }

    public static final int MTU = 125;

    public String base93Encode(byte[] input) throws CodecException {
        int blocks = input.length / 4 + (input.length % 4 == 0 ? 0 : 1);
        StringBuilder sb = new StringBuilder(blocks * 5);
        for(int i = 0; i < blocks; i++) {
            sb.append(this.blockEncode(input, i * 4));
        }
        if(sb.length() > MTU) throw new MTUExceededException();
        return sb.toString();
    }

    public byte[] base93Decode(String input) throws CodecException {
        if(input.length() > MTU) throw new MTUExceededException();
        if(input.length() % 5 != 0) throw new Base93UnalignedException();
        int blocks = input.length() / 5;
        byte[] output = new byte[blocks * 4];
        for(int i = 0; i < blocks; i++) {
            byte[] output1 = this.blockDecode(input, i * 5);
            System.arraycopy(output1, 0, output, i * 4, 4);
        }
        return output;
    }

    private char encodeSymbol(int input) {
        if(input < 0 || input > 93) throw new IllegalArgumentException();
        int ch = '!' + input;
        if(ch >= '+') ch++;
        return (char)ch;
    }

    private int decodeSymbol(char ch) {
        if(ch < ' ' || ch > '~' || ch == '+') throw new IllegalArgumentException();
        int output = ch;
        if(output > '+') output--;
        return output - '!';
    }

    private String blockEncode(byte[] input, int index) {
        long bigint = 0;
        try {
            bigint += Byte.toUnsignedLong(input[index]);
            bigint += Byte.toUnsignedLong(input[index + 1]) << 8;
            bigint += Byte.toUnsignedLong(input[index + 2]) << 16;
            bigint += Byte.toUnsignedLong(input[index + 3]) << 24;
        }
        catch(IndexOutOfBoundsException ignored) {
            // End-of-input region is treated / padded as 0
        }
        StringBuilder sb = new StringBuilder(5);
        sb.append(this.encodeSymbol((int)(bigint % 93L))); bigint /= 93L;
        sb.append(this.encodeSymbol((int)(bigint % 93L))); bigint /= 93L;
        sb.append(this.encodeSymbol((int)(bigint % 93L))); bigint /= 93L;
        sb.append(this.encodeSymbol((int)(bigint % 93L))); bigint /= 93L;
        sb.append(this.encodeSymbol((int)(bigint)));
        return sb.toString();
    }

    private byte[] blockDecode(String input, int index) {
        long bigint = 0;
        try {
            long weight = 93L;
            bigint += this.decodeSymbol(input.charAt(index));
            bigint += this.decodeSymbol(input.charAt(index + 1)) * weight; weight *= 93L;
            bigint += this.decodeSymbol(input.charAt(index + 2)) * weight; weight *= 93L;
            bigint += this.decodeSymbol(input.charAt(index + 3)) * weight; weight *= 93L;
            bigint += this.decodeSymbol(input.charAt(index + 4)) * weight;
        }
        catch(IndexOutOfBoundsException ignored) {
            // Unaligned input is treated as 0
        }
        byte[] output = new byte[4];
        output[0] = (byte)bigint;
        output[1] = (byte)(bigint >> 8);
        output[2] = (byte)(bigint >> 16);
        output[3] = (byte)(bigint >> 24);
        return output;
    }

}

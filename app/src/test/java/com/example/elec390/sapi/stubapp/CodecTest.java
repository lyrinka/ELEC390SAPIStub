package com.example.elec390.sapi.stubapp;

import com.example.elec390.sapi.protocol.PacketBase;
import com.example.elec390.sapi.protocol.PacketDirection;
import com.example.elec390.sapi.protocol.PacketType;
import com.example.elec390.sapi.protocol.codec.PacketCodecManager;
import com.example.elec390.sapi.protocol.codec.exception.CodecException;
import com.example.elec390.sapi.protocol.codec.impl.Base93Helper;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Predicate;

public class CodecTest {

    @Test
    public void base93CoreTest() {
        Predicate<byte[]> predicate = bytes -> {
            System.out.println("Input: " + Arrays.toString(bytes));
            try {
                String encoded = Base93Helper.getInstance().base93Encode(bytes);
                System.out.println("Encoded: +" + encoded + "+");
                byte[] decoded = Base93Helper.getInstance().base93Decode(encoded);
                System.out.println("Decoded: " + Arrays.toString(decoded));
                boolean passed = true;
                for(int i = 0; i < bytes.length; i++) {
                    if (bytes[i] != decoded[i]) {
                        passed = false;
                        break;
                    }
                }
                if(!passed) System.out.println("TEST FAILED");
                System.out.println();
                return passed;
            }
            catch(CodecException ex) {
                ex.printStackTrace();
                return false;
            }
        };

        byte[] aligned1 = new byte[] {0x12, 0x34, 0x56, 0x78};
        byte[] aligned2 = new byte[] {0x12, 0x34, 0x56, 0x78, 0x12, 0x34, 0x56, 0x78};
        byte[] unaligned1 = new byte[] {0x12, 0x34, 0x56, 0x78, (byte)0xAB};

        boolean passed = true;
        passed &= predicate.test(aligned1);
        passed &= predicate.test(aligned2);
        passed &= predicate.test(unaligned1);

        Assert.assertTrue(passed);
    }

    @Test
    public void packetCodecTest() {
        Predicate<PacketBase> predicate = packetBase -> {
            try {
                System.out.println("Input: " + packetBase);
                String encoded = PacketCodecManager.getInstance().getCodec().encode(packetBase);
                System.out.println("Encoded: " + encoded);
                byte[] base93 = Base93Helper.getInstance().base93Decode(encoded);
                System.out.println("Base93: " + Arrays.toString(base93));
                PacketBase decoded = PacketCodecManager.getInstance().getCodec().decode(encoded);
                System.out.println("Decoded: " + decoded);
                boolean passed = packetBase.getPacketType().equals(decoded.getPacketType()) && Arrays.equals(packetBase.getPayload(), decoded.getPayload());
                if(!passed) System.out.println("TEST FAILED");
                System.out.println();
                return passed;
            }
            catch(CodecException ex) {
                ex.printStackTrace();
                return false;
            }
        };
        boolean passed = true; 
        passed &= predicate.test(new PacketBase(new PacketType(PacketDirection.IN, (byte)0x7F), new byte[]{0x12, 0x34, 0x56}));
        passed &= predicate.test(new PacketBase(new PacketType(PacketDirection.OUT, (byte)0x01), new byte[]{0x12, 0x34, 0x56, (byte)0xFF}));
        Assert.assertTrue(passed);
    }

}

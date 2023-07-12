package app.uvtracker.sensor.test;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import app.uvtracker.sensor.protocol.codec.helper.CRC8Helper;

public class CRC8Test {

    @Test
    public void crc8Test() {
        this.crc8Test(new byte[] {
            0x31, 0x32, 0x33,
        }, (byte)0x62);
        this.crc8Test(
                "u2YT?8D%L!|.QaMMws*56f9\"GfbSq7'WkTP/j:RQ4&3fM0^mpVOw|HS|<?F.j\\^@LSn5D6<#NqDpvg#;=/*3KUu_"
                        .getBytes(StandardCharsets.US_ASCII), (byte)0xF9);
    }

    private void crc8Test(byte[] input, byte expectedCRC) {
        Assert.assertEquals(CRC8Helper.compute(input), expectedCRC);
    }

}

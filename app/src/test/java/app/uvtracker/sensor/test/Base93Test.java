package app.uvtracker.sensor.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import app.uvtracker.sensor.protocol.codec.helper.Base93Helper;

public class Base93Test {

    private final Random random;

    public Base93Test() {
        this.random = ThreadLocalRandom.current();
    }

    private byte[] randomBytes(int maxlen) {
        int length = this.random.nextInt(maxlen);
        byte[] bytes = new byte[length];
        this.random.nextBytes(bytes);
        return bytes;
    }

    @Test
    public void base93Test() {
        this.base93Test(new byte[0]);
        this.base93Test(new byte[]{0x12, 0x34, 0x56});
        for (int i = 0; i < 50; i++) {
            this.base93Test(this.randomBytes(500));
        }
    }

    private void base93Test(byte[] input) {
        System.out.println("Encoding test of " + Arrays.toString(input));
        String encode = Base93Helper.encode(input);
        System.out.println("Encoded " + encode);
        byte[] decode = Base93Helper.decode(encode);
        int blocks = input.length / 4 + (input.length % 4 == 0 ? 0 : 1);
        input = Arrays.copyOf(input, blocks * 4);
        Assert.assertArrayEquals(input, decode);
    }

}

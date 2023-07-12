package app.uvtracker.sensor.test;

import org.junit.Assert;
import org.junit.Test;

import app.uvtracker.sensor.protocol.codec.IPacketCodec;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.packet.Packet;
import app.uvtracker.sensor.protocol.type.PacketType;

public class CodecTest {
    @Test
    public void codecTest() {
        this.codecTest(new Packet(
                PacketType.OUT.BUZZ,
                new byte[0]
        ));
        this.codecTest(new Packet(
                PacketType.IN.DATA_SINGLE,
                new byte[] {
                        0x12, 0x34, 0x56,
                }
        ));
    }

    private void codecTest(Packet packet) {
        try {
            System.out.println("Testing " + packet);
            String str = IPacketCodec.get().encode(packet);
            System.out.println("Encoded " + str);
            Packet decoded = IPacketCodec.get().decode(str);
            Assert.assertEquals(packet.getType(), decoded.getType());
            Assert.assertArrayEquals(packet.getPayload(), decoded.getPayload());
        } catch (CodecException e) {
            throw new RuntimeException(e);
        }
    }


}

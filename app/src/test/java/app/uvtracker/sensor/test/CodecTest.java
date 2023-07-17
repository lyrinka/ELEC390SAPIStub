package app.uvtracker.sensor.test;

import org.junit.Assert;
import org.junit.Test;

import app.uvtracker.sensor.protocol.codec.IPacketCodec;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.type.PacketType;

public class CodecTest {
    @Test
    public void codecTest() {
       this.codecTest(new Packet(
                PacketType.OUT.KEEP_ALIVE,
                new byte[0]
        ));
        this.codecTest(new Packet(
                PacketType.IN.DEBUG,
                new byte[] {
                        0x61, 0x62, 0x63,
                }
        ));
    }

    private void codecTest(Packet packet) {
        try {
            System.out.println("Testing " + packet);
            String str = IPacketCodec.get().encode(packet);
            System.out.println("Encoded " + str);
            Packet decoded = IPacketCodec.get().decode(str);
            System.out.println("Decoded " + decoded);
            Assert.assertEquals(packet.getType(), decoded.getType());
            Assert.assertArrayEquals(packet.getPayload(), decoded.getPayload());
        } catch (CodecException e) {
            throw new RuntimeException(e);
        }
    }


}

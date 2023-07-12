package app.uvtracker.sensor.test;

import android.os.ParcelUuid;

import org.junit.Test;

public class MiscTest {

    @Test
    public void test1() {
        ParcelUuid uuid = ParcelUuid.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

        System.out.println(uuid.toString());
    }

}

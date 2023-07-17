package app.uvtracker.sensor.pii.connection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

public class BLEOptions {

    public static boolean TRACE_ENABLED = true;

    public static class Scanner {
        public static boolean RESTRICTED = true;
        public static UUID FILTER_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    }

    public static class Device {
        public static int REQUEST_MTU = 128;
        public static int REQUIRE_MTU = 128;
        public static UUID SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
        public static class Serial {
            public static class Read {
                public static UUID ENDPOINT = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
                public static int PROPERTY = BluetoothGattCharacteristic.PROPERTY_NOTIFY;
                public static UUID DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                public static byte[] DESCRIPTOR_VAL = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            }
            public static class Write {
                public static UUID ENDPOINT = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
                public static int PROPERTY = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
                public static int WRITE_TYPE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
            }
        }
    }

    public static class Connection {
        public static int CONNECTION_TIMEOUT = 8000;
        public static int CONNECTION_GRACE_PERIOD = 500;
        public static int DISCONNECTION_TIMEOUT = 2000;
    }

}

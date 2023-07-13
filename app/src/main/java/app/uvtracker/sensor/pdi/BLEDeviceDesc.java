package app.uvtracker.sensor.pdi;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

@SuppressWarnings("CanBeFinal")
public class BLEDeviceDesc {

    public static final String CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static boolean RESTRICTED = true;

    public static String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    public static String READ_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    public static int READ_CHARACTERISTIC_PROPERTY = BluetoothGattCharacteristic.PROPERTY_NOTIFY;

    public static byte[] READ_CHARACTERISTIC_CCCD_VAL = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

    public static String WRITE_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    public static int WRITE_CHARACTERISTIC_PROPERTY = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;

    public static int WRITE_CHARACTERISTIC_TYPE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

    public static int REQUEST_MTU = 128;

}

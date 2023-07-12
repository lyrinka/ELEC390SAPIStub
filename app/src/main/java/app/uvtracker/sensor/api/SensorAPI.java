package app.uvtracker.sensor.api;

import android.app.Activity;

import java.util.function.BooleanSupplier;

import app.uvtracker.sensor.api.exception.BluetoothException;
import app.uvtracker.sensor.pdi.android.AndroidBLEScanner;

public final class SensorAPI {

    private static SensorAPI instance;

    public static SensorAPI getInstance() {
        if(SensorAPI.instance == null)
            SensorAPI.instance = new SensorAPI();
        return SensorAPI.instance;
    }

    private SensorAPI() {

    }

    public IScanner getAndroidBLEScanner(Activity activity) throws BluetoothException {
        return new AndroidBLEScanner(activity);
    }

    public IScanner getAndroidBLEScannerWithPermCheck(Activity activity, BooleanSupplier checker) throws BluetoothException {
        return new AndroidBLEScanner(activity, checker);
    }

}

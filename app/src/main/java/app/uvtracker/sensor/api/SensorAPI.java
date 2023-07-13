package app.uvtracker.sensor.api;

import android.app.Activity;

import java.util.function.BooleanSupplier;

import app.uvtracker.sensor.api.exception.TransceiverException;
import app.uvtracker.sensor.api.scanner.IScanner;
import app.uvtracker.sensor.pdi.android.scanner.AndroidBLEScanner;

public final class SensorAPI {

    private static SensorAPI instance;

    public static SensorAPI getInstance() {
        if(SensorAPI.instance == null)
            SensorAPI.instance = new SensorAPI();
        return SensorAPI.instance;
    }

    private SensorAPI() {

    }

    public IScanner getAndroidBLEScanner(Activity activity) throws TransceiverException {
        return new AndroidBLEScanner(activity);
    }

    public IScanner getAndroidBLEScannerWithPermCheck(Activity activity, BooleanSupplier checker) throws TransceiverException {
        return new AndroidBLEScanner(activity, checker);
    }

}

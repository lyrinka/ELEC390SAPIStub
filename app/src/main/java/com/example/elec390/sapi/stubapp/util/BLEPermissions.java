package com.example.elec390.sapi.stubapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

public class BLEPermissions {

    public static void ensure(Activity activity) {
        if(activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) return;
        activity.requestPermissions(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
        }, 1);
    }

}

package com.example.elec390.sapi.stubapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.elec390.sapi.ISensorManager;
import com.example.elec390.sapi.impl.android.AndroidBLESensorManager;

public class MainActivity extends AppCompatActivity {

    private ISensorManager sensorManager;

    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(this.sensorManager == null)
            this.sensorManager = new AndroidBLESensorManager(this);

        this.textview = this.findViewById(R.id.txt_display);

        this.findViewById(R.id.btn_scan).setOnClickListener(v -> {
            this.textview.setText("");
            this.sensorManager.startScanning(sensor -> this.textview.setText(this.textview.getText() + "\n" + sensor.toString()));
        });

        this.findViewById(R.id.btn_stop).setOnClickListener(v -> this.sensorManager.stopScanning());

    }

}


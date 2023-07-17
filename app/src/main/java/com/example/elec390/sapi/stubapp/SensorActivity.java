package com.example.elec390.sapi.stubapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Objects;

import app.uvtracker.sensor.api.event.EventHandler;
import app.uvtracker.sensor.api.event.IEventListener;
import app.uvtracker.sensor.pii.connection.AndroidBLESensorConnection;
import app.uvtracker.sensor.pii.connection.ConnectionStageChangeEvent;
import app.uvtracker.sensor.pii.connection.ISensorConnection;

public class SensorActivity extends AppCompatActivity implements IEventListener {

    @NonNull
    private static final String TAG = SensorActivity.class.getSimpleName();

    @Nullable
    private ISensorConnection sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        if(IntentDataHelper.sensor == null) this.finish();
        this.sensor = new AndroidBLESensorConnection(IntentDataHelper.sensor, this);

        TextView text = this.findViewById(R.id.sensor_txt_disp);
//      text.setText(this.sensor.getName());

        this.sensor.registerListener(this);


        Button btnConnect = this.findViewById(R.id.sensor_btn_con);
        Button btnDisconnect = this.findViewById(R.id.sensor_btn_dis);
        Button btnReset = this.findViewById(R.id.sensor_btn_rst);
        btnConnect.setOnClickListener(v -> {
            if(this.sensor.connect()) {
                Log.d(TAG, "Button: Initiated connection flow.");
            }
            else Log.d(TAG, "Button: Connection flow initiation request ignored.");
        });
        btnDisconnect.setOnClickListener(v -> {
            if(this.sensor.disconnect()) {
                Log.d(TAG, "Button: Initiated disconnection.");
            }
            else Log.d(TAG, "Button: Disconnection initiation request ignored.");
        });
        btnReset.setOnClickListener(v -> {
            this.sensor.reset();
            Log.d(TAG, "Button: Connection flow force reset performed.");
        });

        Button btnTest = this.findViewById(R.id.sensor_btn_test);
        btnTest.setOnClickListener((v) -> this.test());
    }

    @EventHandler
    public void onConnectionStatusChange(ConnectionStageChangeEvent event) {
        String status = event.getStage() + " " + event.getPercentage() + "%";
        Log.d(TAG, ">>> Callback: " + status);
        this.updateStatus(">>> " + status);
    }

    private void updateStatus(String msg) {
        TextView textState = this.findViewById(R.id.sensor_txt_state);
        StringBuilder sb = new StringBuilder();
        Date date = new Date();
        sb.append(date.getHours()).append(":");
        sb.append(date.getMinutes()).append(":");
        sb.append(date.getSeconds()).append(".");
        sb.append(date.getTime() % 1000).append("\n");
        sb.append(msg);
        textState.setText(sb.toString());
    }

    private void test() {
        byte[] buffer = new byte[1000];
        for(int i = 0; i < buffer.length; i++)
            buffer[i] = '$';
        Objects.requireNonNull(this.sensor).write(buffer);
    }

}
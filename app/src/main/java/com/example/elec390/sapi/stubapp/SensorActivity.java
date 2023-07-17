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

import app.uvtracker.sensor.pdi.androidble.AndroidBLESensorImpl;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.ISensorConnection;
import app.uvtracker.sensor.pii.connection.packet.ISensorPacketConnection;
import app.uvtracker.sensor.pii.connection.packet.event.PacketReceivedEvent;
import app.uvtracker.sensor.pii.connection.packet.event.UnrecognizableMessageReceivedEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.protocol.packet.Packet;
import app.uvtracker.sensor.protocol.packet.PacketOutBuzz;

public class SensorActivity extends AppCompatActivity implements IEventListener {

    @NonNull
    private static final String TAG = SensorActivity.class.getSimpleName();

    @Nullable
    private ISensor sensor;

    @Nullable
    private ISensorConnection connection;

    @Nullable
    private ISensorPacketConnection testPacketConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        if(IntentDataHelper.sensor == null) this.finish();

        this.sensor = IntentDataHelper.sensor;
        this.connection = sensor.getConnection();
        this.connection.registerListener(this);
        ((AndroidBLESensorImpl)this.sensor).getFactoryBuilds().packetBased.registerListener(this);

        TextView text = this.findViewById(R.id.sensor_txt_disp);
        text.setText(this.sensor.getName());

        Button btnConnect = this.findViewById(R.id.sensor_btn_con);
        Button btnDisconnect = this.findViewById(R.id.sensor_btn_dis);
        Button btnReset = this.findViewById(R.id.sensor_btn_rst);
        btnConnect.setOnClickListener(v -> {
            if(this.connection.connect()) {
                Log.d(TAG, "Button: Initiated connection flow.");
            }
            else Log.d(TAG, "Button: Connection flow initiation request ignored.");
        });
        btnDisconnect.setOnClickListener(v -> {
            if(this.connection.disconnect()) {
                Log.d(TAG, "Button: Initiated disconnection.");
            }
            else Log.d(TAG, "Button: Disconnection initiation request ignored.");
        });
        btnReset.setOnClickListener(v -> {
            this.connection.reset();
            Log.d(TAG, "Button: Connection flow force reset performed.");
        });

        Button btnTest = this.findViewById(R.id.sensor_btn_test);
        btnTest.setOnClickListener((v) -> this.test());
    }

    @EventHandler // Source: ISensorConnection
    public void onConnectionStatusChange(ConnectionStateChangeEvent event) {
        String status = event.getStage() + " " + event.getPercentage() + "%";
        Log.d(TAG, ">>> Callback: " + status);
        this.updateStatus(">>> " + status);
    }

    @EventHandler // Source: ISensorPacketConnection
    public void onPacketReceived(PacketReceivedEvent event) {
        Log.d(TAG, "Received packet " + event.getPacket());
        this.updateStatus(event.getPacket().toString());
    }

    @EventHandler // Source: ISensorPacketConnection
    public void onGarbageReceived(UnrecognizableMessageReceivedEvent event) {
        Log.d(TAG, "Received garbage " + event.getMessageAsUnicode());
        this.updateStatus(event.getMessageAsUnicode());
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
        Packet packet = new PacketOutBuzz();
        Objects.requireNonNull(this.testPacketConnection).write(packet);
    }

}
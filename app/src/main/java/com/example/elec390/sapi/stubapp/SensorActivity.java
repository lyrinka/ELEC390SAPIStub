package com.example.elec390.sapi.stubapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.Objects;

import app.uvtracker.sensor.api.IPacketDrivenSensor;
import app.uvtracker.sensor.api.ISensor;
import app.uvtracker.sensor.protocol.packet.Packet;
import app.uvtracker.sensor.protocol.packet.PacketType;

public class SensorActivity extends AppCompatActivity {

    @NonNull
    private static final String TAG = SensorActivity.class.getSimpleName();

    @Nullable
    private ISensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        this.sensor = IntentDataHelper.sensor;
        if(this.sensor == null) this.finish();

        TextView text = this.findViewById(R.id.sensor_txt_disp);
        text.setText(this.sensor.getName());

        this.sensor.registerConnectionStatusCallback((status) -> {
            Log.d(TAG, ">>> Callback: " + status.toString());
            this.updateStatus(">> " + status);
        });

        IPacketDrivenSensor packetDrivenSensor = (IPacketDrivenSensor)sensor;
        packetDrivenSensor.registerPacketReceptionCallback((packet) ->
                (new Handler(Looper.getMainLooper()))
                .post(() -> this.updateStatus("Received: \n" + packet.toString()))
        );

        Button btnConnect = this.findViewById(R.id.sensor_btn_con);
        Button btnDisconnect = this.findViewById(R.id.sensor_btn_dis);
        Button btnReset = this.findViewById(R.id.sensor_btn_rst);
        btnConnect.setOnClickListener(v -> {
            if(this.sensor.connect()) {
                this.updateStatus(">> Connecting...");
                Log.d(TAG, "Button: Initiated connection flow.");
            }
            else Log.d(TAG, "Button: Connection flow initiation request ignored.");
        });
        btnDisconnect.setOnClickListener(v -> {
            if(this.sensor.disconnect()) {
                this.updateStatus(">> Disconnecting..");
                Log.d(TAG, "Button: Initiated disconnection.");
            }
            else Log.d(TAG, "Button: Disconnection initiation request ignored.");
        });
        btnReset.setOnClickListener(v -> {
            this.sensor.forceReset();
            Log.d(TAG, "Button: Connection flow force reset performed.");
        });

        Button btnTest = this.findViewById(R.id.sensor_btn_test);
        btnTest.setOnClickListener(v -> {
            this.testSendPacket();
            Log.d(TAG, "Button: Test performed.");
        });
    }

    private void testSendPacket() {
        Packet packet = new Packet(
                PacketType.OUT.BUZZ,
                new byte[] {
                        0x12, 0x34, 0x56,
                }
        );
        Log.d(TAG, "Prepared packet: " + packet);

        IPacketDrivenSensor sensor = (IPacketDrivenSensor)this.sensor;
        if(Objects.requireNonNull(sensor).sendPacket(packet))
            this.updateStatus("Sent: \n" + packet);
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

}
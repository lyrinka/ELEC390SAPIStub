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

// TODO: For testing purposes we'll have to invoke PDIs here. However, application shall normally NEVER invoke PDI classes.
import app.uvtracker.sensor.pdi.androidble.AndroidBLESensorImpl;

import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.ISensorConnection;
import app.uvtracker.sensor.pii.connection.packet.event.PacketReceivedEvent;
import app.uvtracker.sensor.pii.connection.packet.event.UnrecognizableMessageReceivedEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.out.PacketOutBuzz;

public class SensorActivity extends AppCompatActivity implements IEventListener {

    @NonNull
    private static final String TAG = SensorActivity.class.getSimpleName();

    @Nullable
    private ISensor sensor;

    @Nullable
    private ISensorConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // We first get the ISensor object passed by the parent activity.
        if(IntentDataHelper.sensor == null) this.finish();

        this.sensor = IntentDataHelper.sensor;
        // We create a connection and register event listeners.
        this.connection = sensor.getConnection();
        this.connection.registerListener(this);
        // Since the application-level sensor interface is still incomplete,
        // for testing purposes, we forcefully pull-out internal handle and register packet listeners.
        // In our end application, only classes within PII (platform independent implementation) shall be used.
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
        // The event object contains current stage, and a recommended percentage to be displayed to the user.
        // The connection process might take 1 to 5 seconds.
        // During connection, the percentage will change from 0 to 100,
        // so the user can see progress instead of waiting for a long time.
        String status = event.getStage() + " " + event.getPercentage() + "%";
        Log.d(TAG, ">>> Callback: " + status);
        this.updateStatus(">>> " + status);
        // Note that in case of failure, there are two states, FAILED_RETRY and FAILED_NO_RETRY.
        // FAILED_RETRY implies the connection failed due to an internal error.
        // - The application should silently retry the connection for a few times before reporting an error to the user.
        // FAILED_NO_RETRY implies the hardware is broken or not supported.
        // - The application should give up and tell the user that the hardware died.
    }

    @EventHandler // Source: ISensorPacketConnection
    public void onPacketReceived(PacketReceivedEvent event) {
        // This should not be exposed to Android application developers.
        // Included here just for testing.
        Log.d(TAG, "Received packet " + event.getPacket());
        this.updateStatus(event.getPacket().toString());
    }

    @EventHandler // Source: ISensorPacketConnection
    public void onGarbageReceived(UnrecognizableMessageReceivedEvent event) {
        // This should not be exposed to Android application developers.
        // Included here just for testing.
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
        // Packet writes should not be exposed to Android application developers.
        // Included here just for testing.
        ((AndroidBLESensorImpl)Objects.requireNonNull(this.sensor)).getFactoryBuilds().packetBased.write(packet);
    }

}
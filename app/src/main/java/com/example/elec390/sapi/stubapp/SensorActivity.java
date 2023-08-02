package com.example.elec390.sapi.stubapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.TimedOpticalRecord;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.ISensorConnection;
import app.uvtracker.sensor.pii.connection.application.event.BatteryInfoEvent;
import app.uvtracker.sensor.pii.connection.application.event.NewEstimationReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncDataReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncProgressChangedEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;

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
        this.connection.unregisterAll();
        this.connection.registerListener(this);

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

    @EventHandler // Source: ISensorConnection
    public void onNewSampleReceived(NewSampleReceivedEvent event) {
        TextView text = this.findViewById(R.id.sensor_txt_meas1);
        text.setText("Data " + event.getSeconds() + ": " + event.getRecord());
    }

    @EventHandler // Source: ISensorConnection
    public void onNewEstimationReceived(NewEstimationReceivedEvent event) {
        TextView text = this.findViewById(R.id.sensor_txt_meas2);
        text.setText("Estimation " + event.getSampleNumber() + ": " + event.getRecord() + " (Int. " + event.getSampleInterval() + ")");
    }

    @EventHandler
    public void onBatteryMeas(BatteryInfoEvent event) {
        Log.d(TAG, ">>> Battery: " + event);
        this.updateStatus("Battery: " + event);
    }

    @EventHandler // Source: ISensorConnection
    public void onSyncProgress(SyncProgressChangedEvent event) {
        String status = "Sync: " + event.getStage() + ": " + event.getProgress();
        Log.d(TAG, ">>> Callback: " + status);
        this.updateStatus(">>> " + status);
    }

    @EventHandler // Source: ISensorConnection
    public void onSyncData(SyncDataReceivedEvent event) {
        List<TimedOpticalRecord> data = event.getData();
        if(data.size() == 0) {
            Log.d(TAG, ">>> Data size: 0.");
        }
        else {
            Log.d(TAG, String.format(">>> Data size: %d. First: %s. Last: %s.", data.size(), data.get(0), data.get(data.size() - 1)));
        }
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
        // TODO: WIP
        Objects.requireNonNull(this.connection);
        if(this.connection.isSyncing()) {
            this.connection.abortSync();
        }
        else {
            this.connection.startSync();
        }
    }

}
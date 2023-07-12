package com.example.elec390.sapi.stubapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;
import java.util.function.Consumer;

import app.uvtracker.sensor.api.IPacketDrivenSensor;
import app.uvtracker.sensor.api.ISensor;
import app.uvtracker.sensor.api.exception.comms.CommunicationException;
import app.uvtracker.sensor.api.exception.comms.ConnectionInactiveException;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.packet.Packet;
import app.uvtracker.sensor.protocol.type.PacketType;

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

        TextView textState = this.findViewById(R.id.sensor_txt_state);

        this.sensor.registerConnectionStatusCallback(new Consumer<ISensor.ConnectionStatus>() {
            @Override
            public void accept(ISensor.ConnectionStatus status) {
                Log.d(TAG, ">>> Callback: " + status.toString());
                textState.setText(status.toString());
            }
        });

        Button btnConnect = this.findViewById(R.id.sensor_btn_con);
        Button btnDisconnect = this.findViewById(R.id.sensor_btn_dis);
        Button btnReset = this.findViewById(R.id.sensor_btn_rst);
        btnConnect.setOnClickListener(v -> {
            if(this.sensor.connect()) Log.d(TAG, "Button: Initiated connection flow.");
            else Log.d(TAG, "Button: Connection flow initiation request ignored.");
        });
        btnDisconnect.setOnClickListener(v -> {
            if(this.sensor.disconnect()) Log.d(TAG, "Button: Initiated disconnection.");
            else Log.d(TAG, "Button: Disconnection initiation request ignored.");
        });
        btnReset.setOnClickListener(v -> {
            this.sensor.forceReset();
            Log.d(TAG, "Button: Connection flow force reset performed.");
        });

        Button btnTest = this.findViewById(R.id.sensor_btn_test);
        btnTest.setOnClickListener(v -> {
            this.test();
            Log.d(TAG, "Button: Test performed.");
        });
    }

    private void test() {
        Packet packet = new Packet(
                PacketType.OUT.BUZZ,
                new byte[] {
                        0x12, 0x34, 0x56,
                }
        );
        Log.d(TAG, "Prepared packet: " + packet);

        IPacketDrivenSensor sensor = (IPacketDrivenSensor)this.sensor;
        try {
            Objects.requireNonNull(sensor).sendPacket(packet);
        } catch(ConnectionInactiveException e) {
            Log.d(TAG, "Exception: connection closed.");
        } catch(CodecException | CommunicationException e) {
            e.printStackTrace();
        }
    }

}
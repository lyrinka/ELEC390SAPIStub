package com.example.elec390.sapi.stubapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import app.uvtracker.sensor.SensorAPI;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;
import app.uvtracker.sensor.pii.scanner.event.SensorScannedEvent;

public class MainActivity extends AppCompatActivity implements IEventListener, Runnable {

    private static final int REFRESH_PERIOD = 500;
    private static final int SCAN_STOP_PERIODS = 60;

    private IScanner scanner;

    private RecyclerViewAdapter listAdapter;

    private List<ISensor> datastore;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private int refreshCounter = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain scanner
        if(this.scanner == null) {
            try {
                this.scanner = SensorAPI.getScanner(this);
                this.scanner.registerListener(this);
            } catch (TransceiverException e) {
                throw new RuntimeException(e);
            }
        }

        this.listAdapter = new RecyclerViewAdapter((sensor) -> {
            IntentDataHelper.sensor = sensor;
            this.stopScanning();
            this.startActivity(new Intent(this.getApplicationContext(), SensorActivity.class));
        });

        RecyclerView rView = this.findViewById(R.id.main_list_sensors);
        rView.setAdapter(this.listAdapter);
        rView.setLayoutManager(new LinearLayoutManager(this));

        Button btn = this.findViewById(R.id.main_btn_scan);
        btn.setText(this.getString(R.string.main_btn_scan));
        btn.setOnClickListener(v -> {
            BLEPermissions.ensure(this);
            if(!this.scanner.isScanning()) this.startScanning();
            else this.stopScanning();
        });
    }

    private void startScanning() {
        Button btn = this.findViewById(R.id.main_btn_scan);
        btn.setText(this.getString(R.string.main_btn_stop));
        this.datastore = null;
        this.refreshCounter = 0;
        try {
            this.scanner.startScanning();
        } catch (TransceiverException e) {
            throw new RuntimeException(e);
        }
        this.run();
    }

    private void stopScanning() {
        Button btn = this.findViewById(R.id.main_btn_scan);
        btn.setText(this.getString(R.string.main_btn_scan));
        try {
            this.scanner.stopScanning();
        } catch (TransceiverException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onScanUpdate(SensorScannedEvent event) {
        this.datastore = new ArrayList<>(event.getSensors());
    }

    @Override
    public void run() {
        if(this.datastore != null) this.listAdapter.updateDatastore(this.datastore);
        else this.listAdapter.clearDatastore();
        if(this.scanner.isScanning()) {
            this.refreshCounter++;
            if(this.refreshCounter > SCAN_STOP_PERIODS) this.stopScanning();
            else this.refreshHandler.postDelayed(this, REFRESH_PERIOD);
        }
    }

}

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int INACTIVE_MS = 4000;
    private static final int REMOVAL_MS = 10000;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @NonNull
        private final View itemView;

        @Nullable
        private ViewContent content;

        @NonNull
        private final Consumer<ISensor> callback;

        public ViewHolder(@NonNull View itemView, @NonNull Consumer<ISensor> callback) {
            super(itemView);
            this.itemView = itemView;
            this.callback = callback;
            itemView.setOnClickListener(this);
        }

        public void updateContent(@NonNull ViewContent content) {
            this.content = content;
            content.applyTo(this.itemView);
        }

        @Override
        public void onClick(View view) {
            if(this.content == null) return;
            this.callback.accept(this.content.getSensor());
        }

    }

    private static class ViewContent {

        @NonNull
        private final ISensor sensor;

        @NonNull
        private final String address;

        @Nullable
        private final String name;

        private final int rssi;

        private final boolean active;

        public ViewContent(@NonNull ISensor sensor, @NonNull String address, @Nullable String name, int rssi, boolean active) {
            this.sensor = sensor;
            this.address = address;
            this.name = name;
            this.rssi = rssi;
            this.active = active;
        }

        public void applyTo(View itemView) {
            TextView nameText = itemView.findViewById(R.id.main_listitem_name);
            TextView addrText = itemView.findViewById(R.id.main_listitem_mac);
            TextView rssiText = itemView.findViewById(R.id.main_listitem_rssi);
            nameText.setText(this.name == null ? "(hidden)" : this.name);
            addrText.setText(this.address);
            rssiText.setText(itemView.getContext().getString(R.string.main_rssi_format, this.rssi));
            int color = itemView.getResources().getColor(this.active ? R.color.sensor_scan_active : R.color.sensor_scan_inactive);
            nameText.setTextColor(color);
            addrText.setTextColor(color);
            rssiText.setTextColor(color);
        }

        @NonNull
        public ISensor getSensor() {
            return this.sensor;
        }

    }

    @NonNull
    private List<ViewContent> datastore;

    @NonNull
    private final Consumer<ISensor> callback;

    public RecyclerViewAdapter(@NonNull Consumer<ISensor> clickCallback) {
        this.datastore = new ArrayList<>();
        this.callback = clickCallback;
    }

    @SuppressLint({"NotifyDataSetChanged"})
    public void updateDatastore(Collection<ISensor> source) {
        long currentTime = new Date().getTime();
        this.datastore = source.stream()
                .filter(sensor -> currentTime - sensor.getLastSeenAt().getTime() < REMOVAL_MS)
                .map(sensor -> new ViewContent(
                        sensor,
                        sensor.getAddress(),
                        sensor.getName(),
                        sensor.getRssi(),
                        currentTime - sensor.getLastSeenAt().getTime() < INACTIVE_MS)
                )
                .sorted(Comparator.comparingInt(o -> -o.rssi))
                .collect(Collectors.toList());
        this.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearDatastore() {
        this.datastore.clear();
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_sensor_item, parent, false), this.callback);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolder)
            ((ViewHolder)holder).updateContent(this.datastore.get(position));
    }

    @Override
    public int getItemCount() {
        return this.datastore.size();
    }

}

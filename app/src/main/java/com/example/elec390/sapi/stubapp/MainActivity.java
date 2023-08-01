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

        /*
            First, obtain device scanner through Sensor API.
            Through the scanner object, you will be able to start and stop scans,
            checking whether the scanner is currently scanning,
            and receive sensor scanned events.
         */
        if(this.scanner == null) {
            try {
                // Pass a context to the API to get appropriate scanner. Usually we just pass the activity as-is.
                // This context will be used for all bluetooth operations, including sensor connection flow.
                this.scanner = SensorAPI.getScanner(this);
            } catch (TransceiverException e) {
                /*
                    TransceiverException is transceiver (bluetooth adapter) related
                    exceptions that has prevented the execution of scanning operations.
                    The three possible sub-exceptions thrown are:
                    - TransceiverUnsupportedException, when the device does not support bluetooth.
                    - TransceiverOffException, when the device bluetooth is off.
                    - TransceiverOffException, when the device bluetooth is off.
                    - TransceiverNoPermException, when the device complains about bluetooth permissions.
                    TransceiverNoPermException may also be thrown from startScanning() and stopScanning() methods.

                    Note that sometimes bluetooth operations silently fail when there's insufficient permission.
                    SAPI does not take care of BLE permissions.
                    Please handle these exceptions to tell the user what is wrong and handle BLE permission requests.
                 */
                throw new RuntimeException(e);
            }
        }

        // We register event handlers. Please see methods annotated with @EventHandler for explanations.
        this.scanner.registerListener(this);

        // We create a recycler view adapter.
        // Here we tell the adapter that if user clicks anything, call gotoSensorActivity()
        // and pass the clicked sensor (type ISensor) as argument.
        this.listAdapter = new RecyclerViewAdapter(this::gotoSensorActivity);
        // We bind the adapter with the recycler view.
        RecyclerView rView = this.findViewById(R.id.main_list_sensors);
        rView.setAdapter(this.listAdapter);
        rView.setLayoutManager(new LinearLayoutManager(this));

        // We find the scan button.
        Button btn = this.findViewById(R.id.main_btn_scan);
        btn.setText(this.getString(R.string.main_btn_scan));
        // We tell the button that once pressed, call toggleScanning()
        btn.setOnClickListener(v -> this.toggleScanning());
    }

    // This method will toggle scanning.
    private void toggleScanning() {
        // Some very rough implementation to get BLE permissions, specific to the phone I'm using.
        // DO NOT reuse this code! (It's not in SAPI anyways.) Please implement something more suited to our application.
        BLEPermissions.ensure(this);
        // isScanning() is provided by scanner interface.
        if(!this.scanner.isScanning()) this.startScanning();
        else this.stopScanning();
    }

    // This method will start scanning and set UI states.
    private void startScanning() {
        // We clear datastore and refresh counter.
        // For list refreshing mechanism, please see run() method.
        this.datastore = null;
        this.refreshCounter = 0;
        try {
            // startScanning() is provided by scanner interface.
            // It throws TransceiverException as well.
            // Once scanning starts, scanner will emit events.
            // Please see methods annotated with @EventHandler for more details.
            this.scanner.startScanning();
            // If the scanner is already scanning, startScanning() will do nothing and return.
            // Same for stopScanning(), it will do nothing when the scanning is already stopped.
        } catch (TransceiverException e) {
            throw new RuntimeException(e);
        }
        this.run();
        // We set button text to stop
        Button btn = this.findViewById(R.id.main_btn_scan);
        btn.setText(this.getString(R.string.main_btn_stop));
    }

    // This method will stop scanning and set UI states.
    private void stopScanning() {
        try {
            // stopScanning() is provided by scanner interface.
            // It throws TransceiverException as well.
            this.scanner.stopScanning();
        } catch (TransceiverException e) {
            throw new RuntimeException(e);
        }
        // We set button text to scan
        Button btn = this.findViewById(R.id.main_btn_scan);
        btn.setText(this.getString(R.string.main_btn_scan));
    }

    // This method will go to sensor activity.
    private void gotoSensorActivity(ISensor sensor) {
        // ISensor is an interface abstracting a scanned hardware sensor.
        // It is possible to obtain name and MAC address as string,
        // RSSI and last time seen during the scan,
        // - RSSI stands for received signal strength indicator, in dBm.
        // - It's usually a negative number and the larger (closer to zero) it is, the better the signal intensity.
        // and create a connection handle for communications.
        // To avoid the hassle of passing sensor data through intent,
        // we essentially use IntentDataHelper as a global variable class to pass references.
        IntentDataHelper.sensor = sensor;
        // We try to stop scanning
        this.stopScanning();
        // We start activity via explicit intent.
        this.startActivity(new Intent(this.getApplicationContext(), SensorActivity.class));
    }

    /*
        Event handling:
        First we discuss event handling itself. An event is essentially an object emitted by a class instance.
        We call event-emitting objects event sources and they implement the IEventSource interface.
        The object will be passed to "listening methods", or event handlers, which is characterized by the following:
        - The method must reside in a class that implements the IEventListener interface.
            This interface is empty.
            It is there to force programmers explicitly state that "this class might contain event handlers" to avoid mistakes.
            This object belonging to a class implementing IEventListener is called an event listener.
        - The method must be annotated by @EventHandler annotation.
        - The method must accept exactly one argument. The type of the argument is called input type.
        - (It doesn't matter whether the handler is public, protected, package or private.)
        - (It is recommended that event handlers return void.)
        There are no restrictions on which class to use as event listeners. It is very flexible.
        Here we are declaring MainActivity class as event listener.
        And now there are two questions:
        - How does the event source object know about event handlers?
            We invoke registerListener(listenerObject), where listenerObject implements IEventListener (to prevent mistakes).
            The event source will start to process every single method in the class of listenerObject,
            and filter out all methods that match the previously stated criteria,
            most importantly, being annotated with @EventHandler. These methods will be stored.
        - When an event (any object) is produced, which handler(s) do we invoke?
            The object will be compared to the "input type" of all handlers.
            For a specific handler, if the event object is assignable to this input type (same class or sub-class),
            the handler will be invoked. Otherwise the handler is not executed.
            It is not safe to assume the execution order of the handlers - it depends on registration order and internal implementation.
        These logic are ultimately implemented in EventRegistry.java (in PII library). Please refer to code for further details.

        The IScanner object we received (scanner) implements IEventSource.
        If we go to IScanner.java, it is stated in comments that IScanner "Emits event: SensorScannedEvent".
        Therefore, instances of SensorScannedEvent will be emitted.
        If we wish to "catch" these objects, our handler's input type must be SensorScannedEvent (or its superclass).

        At the beginning of the code we registered this activity object as event listener.
        Internal logic will then scan through all methods of MainActivity and finds onScanUpdate() to be an event handler.
        When the BLE scanner scans / updates a sensor, an instance of SensorScannedEvent will be emitted.
        Since the input type of onScanUpdate() matches this event object, the handler below is called.
     */
    @EventHandler
    public void onScanUpdate(SensorScannedEvent event) {
        // SensorScannedEvent contains information related to a sensor being scanned/updated.
        // getSensor() returns the scanned sensor related to this event, with type ISensor, which is the abstraction of a hardware sensor entity.
        // getSensors() returns a list of all scanned sensors.
        // - Be careful not to modify the returned collection as the collection is not copied. There are no restrictions on read access.
        // isFirstTime() returns whether this is the first time this sensor is discovered.
        // Here we are only interested in the list of scanned sensors.
        this.datastore = new ArrayList<>(event.getSensors());
        /*
            More on ISensor:
            ISensor represents a hardware sensor entity. (NOT a connection!)
            The entity is described by a MAC address and a name (may be empty).
            The entity also contains the RSSI and last time seen from a scan.
            In order to start a communication, we would need to obtain its connection handle by invoking getConnection().
         */
    }

    /*
        This is our list refresh handler (irrelevant to BLE library).
        The handler is triggered by manually invoking run() at start of scan.
     */
    @Override
    public void run() {
        // Update list adapter
        if(this.datastore != null) this.listAdapter.updateDatastore(this.datastore);
        else this.listAdapter.clearDatastore();
        // If the scanner is still scanning:
        if(this.scanner.isScanning()) {
            // Increase counter
            this.refreshCounter++;
            // If the counter is large enough, we stop scanning (scan timeout).
            if(this.refreshCounter > SCAN_STOP_PERIODS) this.stopScanning();
            // Otherwise, we schedule another list update some time after.
            else this.refreshHandler.postDelayed(this, REFRESH_PERIOD);
        }
        // If the scanner is not scanning, the method returns without scheduling another execution.
        // Therefore list refreshing stops.
    }

}

// Standard recycler view adapter.
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
        // Here we process incoming data by:
        // - removing sensors that are inactive for too long
        // - greying out sensors that are inactive for a bit
        // - sort all sensors according to RSSI
        // (in real application, if a list of sensors is ever surfaced to the user, please sort by name, as RSSI changes and user might be confused)
        // (alternatively, the application can scan sensors for a few seconds and,
        //      - if there's only one sensor, connect to this sensor without bothering the user of anything,
        //      - if there are multiple sensors, ask the user to manually select one.)
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

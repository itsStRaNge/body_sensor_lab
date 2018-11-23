package com.lukas.body_sensor_lab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.rcsexample.bsnlib.BluetoothConnectionService;
import com.rcsexample.bsnlib.ControlPacket;
import com.rcsexample.bsnlib.Data;
import com.rcsexample.bsnlib.DataProvider;
import com.rcsexample.bsnlib.DeviceListActivity;


@SuppressLint("Registered")
public  class SensorHandler extends AppCompatActivity implements BluetoothConnectionService.BluetoothConnectionListener {
    private final String TAG = "SensorHandler";

    protected static final int INTENT_REQUEST_CHOOSE_DEVICE = 1;
    private BluetoothConnectionService m_bluetooth_service = null;
    private BluetoothAdapter m_bluetooth_adapter = null;
    private String m_device_address = "";
    private BluetoothDevice m_device;
    private DataProvider m_data_provider;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        m_bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
        m_bluetooth_service = new BluetoothConnectionService();
        m_bluetooth_service.registerListener(this);
        m_data_provider = new DataProvider(getApplicationContext());
        m_data_provider.registerListener(onDataAvailableListener);
        // turn bluetooth on
        if (!m_bluetooth_adapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onPause() {
        m_data_provider.disableBluetoothSensorProviding();
        m_data_provider.disablePhoneSensorProviding();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_data_provider.enableBluetoothSensorProviding(m_bluetooth_service);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_CHOOSE_DEVICE) {
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                m_device_address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                if (BluetoothAdapter.checkBluetoothAddress(m_device_address)) {
                    m_device = m_bluetooth_adapter.getRemoteDevice(m_device_address);
                    m_bluetooth_service.connect(m_device);
                    m_data_provider.enableBluetoothSensorProviding(m_bluetooth_service);
                }
            }
        }
    }

    protected void connect(){
        if (m_bluetooth_adapter.getState() != BluetoothAdapter.STATE_ON) {
            makeToast("Bluetooth not available");
        } else {
            // set up new connection now - start by choosing the
            // remote device (next: onActivityResult())
            Intent chooseDeviceIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
            startActivityForResult(chooseDeviceIntent, INTENT_REQUEST_CHOOSE_DEVICE);
        }
    }

    protected void disconnect(){
        m_bluetooth_service.stop();
    }

    protected void start_streaming(){
        new ControlPacket(ControlPacket.INSTRUCTION_START_STOP_MEASURE_SEND_BT)
                .build().send(m_bluetooth_service);

    }

    protected void stop_streaming(){
        (new ControlPacket(ControlPacket.INSTRUCTION_SLEEP)).build().send(m_bluetooth_service);
    }

    @Override
    public void onBluetoothConnectionStateChanged(int state) {
        String text = "";
        if (state == BluetoothConnectionService.STATE_CONNECTED)
            text = "Connected.";
        else if (state == BluetoothConnectionService.STATE_CONNECTING)
            text = "Connecting...";
        else if (state == BluetoothConnectionService.STATE_NONE) text = "No connection.";
        makeToast(text);
    }

    @Override
    public void onBluetoothConnectionConnected(String name, String address) {
        makeToast("Connected to: " + name + " (" + address + ")");
    }

    @Override
    public void onBluetoothConnectionFailure(int whatFailed) {
        String text = "";
        if (whatFailed == BluetoothConnectionService.FAILURE_CONNECTION_LOST)
            text = "Bluetooth connection lost";
        else if (whatFailed == BluetoothConnectionService.FAILURE_WRITE_FAILED)
            text = "Bluetooth write failed";
        else if (whatFailed == BluetoothConnectionService.FAILURE_CONNECTING_FAILED) text = "Bluetooth connecting failed";
        makeToast(text);
    }
    @Override
    public void onBluetoothConnectionReceive(byte[] buffer, int numberOfBytesInBuffer) {

    }

    @Override
    public void onBluetoothConnectionWrite(byte[] buffer) {

    }

    /** display toast message. */
    private void makeToast(String text) {
        runOnUiThread(new ToastMaker(text));
    }

    /**
     * used by ... to display a toast. Must
     * be run on the UI thread
     */
    private class ToastMaker implements Runnable {
        private String text;

        public ToastMaker(String text) {
            this.text = text;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The method in this listener gets called when the DataProvider has new
     * Data available.
     */
    private final DataProvider.OnDataAvailableListener onDataAvailableListener = new DataProvider.OnDataAvailableListener() {
        @Override
        public synchronized void onDataAvailable(Data data) {
            Log.d(TAG, "Data Recieved yes");
            /*
            // add the samples in data to the plots and our value arrays
            for (int i = 0; i < data.getNrOfSamples(); i++) {
                accSeriesX.addLast(data.getT().get(i), data.getX().get(i));
                accSeriesY.addLast(data.getT().get(i), data.getY().get(i));
                accSeriesZ.addLast(data.getT().get(i), data.getZ().get(i));

                accX.addAll(data.getX());
                accY.addAll(data.getY());
                accZ.addAll(data.getZ());
            }

            // trim plots to maximum size
            while (accSeriesX.size() > PLOT_HISTORY_SIZE)
                accSeriesX.removeFirst();
            while (accSeriesY.size() > PLOT_HISTORY_SIZE)
                accSeriesY.removeFirst();
            while (accSeriesZ.size() > PLOT_HISTORY_SIZE)
                accSeriesZ.removeFirst();
            while (accX.size() > PLOT_HISTORY_SIZE)
                accX.remove(0);
            while (accY.size() > PLOT_HISTORY_SIZE)
                accY.remove(0);
            while (accZ.size() > PLOT_HISTORY_SIZE)
                accZ.remove(0);

            // calculate the spectrum
            getFrequencies();

            // update the plot
            accPlot.redraw();*/
        }
    };
}

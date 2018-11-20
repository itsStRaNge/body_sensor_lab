package com.lukas.body_sensor_lab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements Switch.OnCheckedChangeListener {
    private final String TAG = "MainActivity";

    private BluetoothAdapter m_bluetooth_adapter;
    private ArrayList<BluetoothDevice> m_paired_devices;
    public final UUID M_UUID = UUID.randomUUID();

    private final byte STREAM_DATA_BLUETOOTH = 0x62; // starts measuring and streams data via bluetooth
    private final byte SLEEP_MODE = 0x78; // stops measuring
    private final byte TRANSMIT_SD_CARD = 0x73; // transmits all stored data on sd card over bluetooth
    private final byte ERASE_SD_CARD = 0x65;
    private final byte START_MEASUREMENT = 0x6D; // starts measureing and stores data on sd card

    public Handler msg_handler;
    public final int CONNECTING_HANLDER_FLAG = 0;
    public final int CONNECTED_HANDLER_FLAG = 1;
    private ConnectingThread m_connecting_thread;
    private ConnectedThread m_connected_thread;

    /**
     * UI Refactoring
     **/
    private LinearLayout m_prod_layout;
    private LinearLayout m_label_layout;

    private Switch m_switch_connect;
    private Switch m_switch_stream;
    private Switch m_switch_labeling;

    private RadioGroup m_radiogroup_labels;

    private ImageView m_prod_img_status;
    private TextView m_prod_txt_status;
    private TextView m_labeling_txt_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_prod_layout = (LinearLayout) findViewById(R.id.layout_productive);
        m_label_layout = (LinearLayout) findViewById(R.id.layout_labeling);

        m_switch_connect = (Switch) findViewById(R.id.switch_connect);
        m_switch_connect.setOnCheckedChangeListener(this);
        m_switch_stream = (Switch) findViewById(R.id.switch_stream);
        m_switch_stream.setOnCheckedChangeListener(this);
        m_switch_labeling = (Switch) findViewById(R.id.switch_labeling);
        m_switch_labeling.setOnCheckedChangeListener(this);

        m_radiogroup_labels = (RadioGroup) findViewById(R.id.radio_group);

        m_prod_img_status = (ImageView) findViewById(R.id.image_status);
        m_prod_txt_status = (TextView) findViewById(R.id.text_status);
        m_labeling_txt_status = (TextView) findViewById(R.id.text_labeling_status);

        m_bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
        m_paired_devices = new ArrayList(m_bluetooth_adapter.getBondedDevices());

        // turn bluetooth on
        if (!m_bluetooth_adapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        }

        init_handler();
    }

    private void init_handler() {
        msg_handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CONNECTED_HANDLER_FLAG) {
                    if (msg.obj != null) {
                        byte[] buffer = (byte[]) msg.obj;
                        Log.d(TAG, "InputStream: " + Arrays.toString(buffer));
                    }
                } else if (msg.what == CONNECTING_HANLDER_FLAG) {
                    if (msg.obj != null) {
                        BluetoothSocket socket = (BluetoothSocket) msg.obj;
                        m_connected_thread = new ConnectedThread(socket, msg_handler);
                        m_connected_thread.start();
                    } else {
                        // TODO add toast that something went wrong
                        m_switch_connect.setChecked(false);
                    }
                }
            }
        };
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.switch_connect:
                if (checked)
                    connect();
                else
                    disconnect();
                break;
            case R.id.switch_labeling:
                if (checked) {
                    m_label_layout.setVisibility(View.VISIBLE);
                    m_prod_layout.setVisibility(View.GONE);
                } else {
                    m_label_layout.setVisibility(View.GONE);
                    m_prod_layout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.switch_stream:
                if (checked)
                    start_streaming();
                else
                    stop_streaming();
                break;
        }
    }

    private void connect(){
        // TODO careful with paired_devices.get(0)
        BluetoothDevice d = m_paired_devices.get(0);
        m_connecting_thread = new ConnectingThread(d, M_UUID, msg_handler);
        m_connecting_thread.start();
    }
    private void disconnect(){
        try {
            m_connected_thread.write(SLEEP_MODE);
            m_connected_thread.cancel();
            m_connecting_thread.cancel();
        }catch(NullPointerException ignored){}
    }
    private void start_streaming(){
        if(m_switch_connect.isChecked())
            m_connected_thread.write(STREAM_DATA_BLUETOOTH);
        // TODO what are we going to do if not connected?
    }
    private void stop_streaming(){
        if(m_switch_connect.isChecked())
            m_connected_thread.write(SLEEP_MODE);
    }

    // TODO lable data base on m_switch_label
    // TODO parse data
    // TODO store data on a database
    // TODO calculate a status and update it
}
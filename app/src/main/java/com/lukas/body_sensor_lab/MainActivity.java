package com.lukas.body_sensor_lab;

import android.annotation.SuppressLint;
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
import android.widget.ListView;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public final UUID M_UUID = UUID.randomUUID();
    private final String TAG = "MainActivity";

    public Handler msg_handler;
    public final int CONNECTING_HANLDER_FLAG = 0;
    public final int CONNECTED_HANDLER_FLAG = 1;

    private BluetoothAdapter m_bluetooth_adapter;
    private ArrayList<BluetoothDevice> m_pairedDevices;
    private ListView m_lv_paired_dev;

    private ConnectingThread m_connecting_thread;
    private ConnectedThread m_connected_thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1 = (Button) findViewById(R.id.button_visible);
        b1.setOnClickListener(this);
        b1 =(Button)findViewById(R.id.button_list_devices);
        b1.setOnClickListener(this);
        b1 =(Button)findViewById(R.id.button_cancle);
        b1.setOnClickListener(this);

        m_bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
        m_lv_paired_dev = (ListView)findViewById(R.id.listView);

        // turn bluetooth on
        if (!m_bluetooth_adapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        }
        list_devices();

        init_handler();
    }

    private void init_handler(){
        msg_handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                if(msg.what == CONNECTED_HANDLER_FLAG){
                    Log.d(TAG,"Received some data");
                }else if(msg.what == CONNECTING_HANLDER_FLAG){
                    if(msg.obj != null){
                        BluetoothSocket socket = (BluetoothSocket) msg.obj;
                        m_connected_thread = new ConnectedThread(socket);
                        m_connected_thread.start();
                    }
                }
            }
        };
    }
    public  void visible(){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }


    public void list_devices(){
        m_pairedDevices = new ArrayList(m_bluetooth_adapter.getBondedDevices());

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, m_pairedDevices);
        m_lv_paired_dev.setAdapter(adapter);
        m_lv_paired_dev.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id){
                BluetoothDevice d = m_pairedDevices.get(pos);
                m_connecting_thread = new ConnectingThread(d, M_UUID);
                m_connecting_thread.start();

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_list_devices:
                list_devices();
                break;
            case R.id.button_visible:
                //visible();
                mConnectedThread.write(null);
                break;
            case R.id.button_cancle:

                break;
        }
    }





}
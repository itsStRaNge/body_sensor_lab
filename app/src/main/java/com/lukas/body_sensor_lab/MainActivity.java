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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
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
    private TextView m_status;

    private ConnectingThread m_connecting_thread;
    private ConnectedThread m_connected_thread;

    private final byte STREAM_DATA_BLUETOOTH = 0x62; // starts measuring and streams data via bluetooth
    private final byte SLEEP_MODE = 0x78; // stops measuring
    private final byte TRANSMIT_SD_CARD = 0x73; // transmits all stored data on sd card over bluetooth
    private final byte ERASE_SD_CARD = 0x65;
    private final byte START_MEASUREMENT = 0x6D; // starts measureing and stores data on sd card

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1 = (Button) findViewById(R.id.button_stream_data);
        b1.setOnClickListener(this);
        b1 =(Button)findViewById(R.id.button_list_devices);
        b1.setOnClickListener(this);
        b1 =(Button)findViewById(R.id.button_disconnect);
        b1.setOnClickListener(this);
        b1 = (Button)findViewById(R.id.button_sleep);
        b1.setOnClickListener(this);

        m_status = (TextView) findViewById(R.id.text_status);

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
                    if(msg.obj != null){
                        byte[] buffer = (byte[]) msg.obj;
                        Log.d(TAG, "InputStream: " + Arrays.toString(buffer));
                    }
                }else if(msg.what == CONNECTING_HANLDER_FLAG){
                    if(msg.obj != null){
                        m_status.setText("Status: connected");

                        BluetoothSocket socket = (BluetoothSocket) msg.obj;
                        m_connected_thread = new ConnectedThread(socket, msg_handler);
                        m_connected_thread.start();
                    }
                }
            }
        };
    }

    public void list_devices(){
        m_pairedDevices = new ArrayList(m_bluetooth_adapter.getBondedDevices());

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, m_pairedDevices);
        m_lv_paired_dev.setAdapter(adapter);
        m_lv_paired_dev.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id){
                BluetoothDevice d = m_pairedDevices.get(pos);
                m_connecting_thread = new ConnectingThread(d, M_UUID, msg_handler);
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
            case R.id.button_stream_data:
                m_connected_thread.write(STREAM_DATA_BLUETOOTH);
                m_status.setText("Status: streaming");
                break;
            case R.id.button_sleep:
                m_connected_thread.write(SLEEP_MODE);
                m_status.setText("Status: sleep");
                break;
            case R.id.button_disconnect:
                m_connected_thread.write(SLEEP_MODE);
                m_connected_thread.cancel();
                m_connecting_thread.cancel();
                m_status.setText("Status: disconnect");
                break;
        }
    }

    /************MENU***************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()){
            case R.id.action_main:
                i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                return true;
            case R.id.action_example:
                i = new Intent(this, ExampleActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
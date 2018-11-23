package com.lukas.body_sensor_lab;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rcsexample.bsnlib.BluetoothConnectionService;
import com.rcsexample.bsnlib.ControlPacket;
import com.rcsexample.bsnlib.Data;
import com.rcsexample.bsnlib.DataProvider;
import com.rcsexample.bsnlib.DeviceListActivity;


public class MainActivity extends SensorHandler implements Switch.OnCheckedChangeListener{
    private final String TAG = "MainActivity";

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
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.switch_connect:
                if (checked) {
                    connect();
                } else {
                    disconnect();
                }
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


    /** MENU **/
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
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
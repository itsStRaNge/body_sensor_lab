package com.lukas.body_sensor_lab;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class ConnectingThread extends MyThread {
    String TAG = "ConnectingThread";

    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;
    private UUID m_uuid;

    public ConnectingThread(BluetoothDevice device, UUID uuid, Handler handler) {
        super(handler);
        mmDevice = device;
        m_uuid = uuid;
    }

    public void run(){
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            tmp = mmDevice.createRfcommSocketToServiceRecord(m_uuid);
        } catch (IOException e) {
            Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
        }

        mmSocket = tmp;

        // Make a connection to the BluetoothSocket
        // TODO still a little bit fishy to call connected(..) twice...
        try {
            mmSocket.connect();
            connected(mmSocket);
        } catch (IOException e) {
            try {
                // dirty workaround
                // https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/25647197#25647197
                Log.d(TAG, "run: Socket connect Fallback.");
                mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                mmSocket.connect();
                connected(mmSocket);
            } catch (Exception e1) {
                try {
                    mmSocket.close();
                    Log.e(TAG, "run Close socket " + e1.getMessage());
                }catch (IOException ell) {
                    Log.e(TAG, "run cannot close socket " + ell.getMessage());
                }
            }
        }

    }
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
        }
    }


    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "CONNECTED !!!");
        Message msg = new Message();
        msg.what = CONNECTING_FLAG;
        msg.obj = mmSocket;
        handler().sendMessage(msg);
    }
}

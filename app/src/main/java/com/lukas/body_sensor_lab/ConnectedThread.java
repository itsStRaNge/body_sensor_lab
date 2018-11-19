package com.lukas.body_sensor_lab;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public class ConnectedThread extends MyThread {
    private boolean m_connection_available = false;

    private final String TAG = "ConnectedThread";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        super(handler);
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run(){
        m_connection_available = true;
        byte[] buffer = new byte[1024];  // buffer store for the stream
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            // Read from the InputStream
            try {
                int bytes = mmInStream.read(buffer);
                // Log.d(TAG, "InputStream: " + Arrays.toString(buffer));
                // TODO maybe some data gets lost here
                Message msg = new Message();
                msg.what = CONNECTED_FLAG;
                msg.obj = buffer;
                handler().sendMessage(msg);
            } catch (IOException e) {
                Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                break;
            }
        }
        m_connection_available = false;
    }

    public void write(byte cmd) {
        if(m_connection_available) {
            try {
                mmOutStream.write(cmd);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }else{
            Log.d(TAG, "Connection not available");
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            m_connection_available = false;
            mmSocket.close();
        } catch (IOException ignored) { }
    }
}

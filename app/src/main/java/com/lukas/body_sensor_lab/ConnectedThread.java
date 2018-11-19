package com.lukas.body_sensor_lab;


import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public class ConnectedThread extends Thread {
    private final String TAG = "ConnectedThread";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
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
        byte[] buffer = new byte[1024];  // buffer store for the stream

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            // Read from the InputStream
            try {
                int bytes = mmInStream.read(buffer);
                Log.d(TAG, "InputStream: " + Arrays.toString(buffer));

                // TODO somehow get info to cloud or ui
            } catch (IOException e) {
                Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        Log.d(TAG, "write: Writing to sensor: " + Arrays.toString(bytes));
        try {
            mmOutStream.write(0x62);
        } catch (IOException e) {
            Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException ignored) { }
    }
}

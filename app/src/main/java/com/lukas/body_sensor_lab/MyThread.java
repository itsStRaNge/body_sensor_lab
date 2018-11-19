package com.lukas.body_sensor_lab;

import android.os.Handler;

public class MyThread  extends Thread {
    public final int CONNECTING_FLAG = 0;
    public final int CONNECTED_FLAG = 1;

    private Handler m_handler;

    public MyThread(Handler handler){
        m_handler = handler;
    }

    public Handler handler(){return m_handler;}
}

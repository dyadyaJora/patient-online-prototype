package com.example.android.patientonline.data;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RunningBtThread extends Thread {

    private final InputStream connectedInputStream;
    private final OutputStream connectedOutputStream;
    BluetoothSocket bluetoothSocket;
    Activity someActivity;

    public interface Callback {
        void dataProcessingCb(RunningBtThread thr, InputStream in);
    }

    Callback callback;
    public String type;

    public RunningBtThread(String type, Activity c, Callback callback) {

        InputStream in = null;
        OutputStream out = null;
        this.type = type;
        this.callback = callback;
        this.someActivity = c;

        bluetoothSocket = BtHelper.getSocket(type);

        try {
            in = bluetoothSocket.getInputStream();
            out = bluetoothSocket.getOutputStream();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        connectedInputStream = in;
        connectedOutputStream = out;
    }

    public void write(byte[] buffer) {
        try {
            connectedOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        callback.dataProcessingCb(this, connectedInputStream);
    }

    public void cancel() {
        someActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(someActivity, "Closed - Bt Socket", Toast.LENGTH_LONG).show();
            }
        });

        BtHelper.closeSocketConnection(type);
    }
}

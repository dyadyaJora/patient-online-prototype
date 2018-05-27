package com.example.android.patientonline.data;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ConnectingBtThread extends Thread {

    private BluetoothSocket bluetoothSocket = null;
    Activity someActivity;
    String type;

    public interface Callback {
        void cb(String type);
        void closeCb();
    }

    Callback callback;

    public ConnectingBtThread(BluetoothDevice device, String type, Activity c, Callback callback) {

        someActivity = c;
        this.type = type;
        this.callback = callback;

        bluetoothSocket = BtHelper.createSocketConnection(device, type);
    }

    @Override
    public void run() { // Коннект
        boolean success = false;

        try {
            synchronized (bluetoothSocket) {
                bluetoothSocket.connect();
            }
            success = true;
        }

        catch (IOException e) {
            e.printStackTrace();

            someActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(someActivity, "Не возможно установить Bluetooth соедненение", Toast.LENGTH_LONG).show();
                }
            });

            BtHelper.closeSocketConnection(type);
            callback.closeCb();
        }

        if(success) {  // Если законнектились, то init device

            someActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // диалог ожидания
                    Toast.makeText(someActivity, "Соединение успешно установлено", Toast.LENGTH_LONG).show();

                }
            });

            // ============== Основное действие ==========================================

            boolean b = BtHelper.isConnected(type);
            callback.cb(type);

            // =================================================================================
        } else {
            callback.closeCb();
        }
    }

    public void cancel() {
        someActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(someActivity, "Closed - Bt Socket 2", Toast.LENGTH_LONG).show();
            }
        });

        BtHelper.closeSocketConnection(type);
        callback.closeCb();
    }
}

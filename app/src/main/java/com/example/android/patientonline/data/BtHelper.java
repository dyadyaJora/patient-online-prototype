package com.example.android.patientonline.data;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class BtHelper {
    public static HashMap<String, BluetoothSocket> btSockets = new HashMap<>();
    final static String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";

//    public static synchronized void addSocket(String type) {
//        btSockets.put(type, null);
//    }

    public static synchronized BluetoothSocket getSocket(String type) {
        return btSockets.get(type);
    }

    public static synchronized boolean checkSocket(String type) {
        return btSockets.containsKey(type);
    }

    public static synchronized void deleteSocket(String type) {
        btSockets.remove(type);
    }

    public static synchronized boolean isConnected(String type) {
        BluetoothSocket s = btSockets.get(type);

        if (s != null) {
            return s.isConnected();
        }

        return false;
    }

    @Nullable
    public static synchronized BluetoothSocket createSocketConnection(BluetoothDevice d, String type) {
        UUID myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        BluetoothSocket bS;
        try {
            bS = d.createRfcommSocketToServiceRecord(myUUID);
            btSockets.put(type, bS);
        }
        catch (IOException e) {
            e.printStackTrace();
            bS = null;
        }
        return bS;
    }

    public static synchronized void closeSocketConnection(String type) {
        try {
            BluetoothSocket bS = btSockets.get(type);
            bS.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

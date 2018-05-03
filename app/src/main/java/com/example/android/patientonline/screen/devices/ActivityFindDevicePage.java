package com.example.android.patientonline.screen.devices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.bluetooth.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.patientonline.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ActivityFindDevicePage extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;

    boolean finding = false;

    BluetoothAdapter bluetoothAdapter;
    SingBroadcastReceiver mReceiver;
    ArrayList<HashMap<String, String>> pairedDeviceArrayList;
    HashMap<String, String> map;
    SimpleAdapter pairedDeviceAdapter;
    ListView listViewPairedDevice;
    public ConnectBtThread connectBtThread;
    public InitBtThread initBtThread;
    UUID myUUID;
    StringBuilder sb = new StringBuilder();

    Button btnFindDev;
    ProgressBar pB;
    TextView tInfo;
    AlertDialog.Builder ad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_device_page);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Подключение");

        final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        btnFindDev = (Button) findViewById(R.id.btnFindDevices);
        btnFindDev.setOnClickListener(this);

        pB = (ProgressBar) findViewById(R.id.progressBarFind);
        pB.setVisibility(View.INVISIBLE);

        tInfo = (TextView) findViewById(R.id.tvDevice);
        listViewPairedDevice = (ListView) findViewById(R.id.lvDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth не поддерживается устройством", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + " " + bluetoothAdapter.getAddress();
        tInfo.setText(String.format("Это устройство: %s", stInfo));

        stopFindNewDevice();
        mReceiver = new SingBroadcastReceiver();
        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, ifilter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            initSetup();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){

            if(resultCode == Activity.RESULT_OK) {
                initSetup();
                Toast.makeText(this, "BlueTooth включён", Toast.LENGTH_SHORT).show();
            }

            else {
                Toast.makeText(this, "BlueTooth не включён", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopFindNewDevice();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFindDevices:
                finding = !finding;
                if (finding) {
                    if (!startfindNewDevice())
                        return;
                    pB.setVisibility(View.VISIBLE);
                    btnFindDev.setText(getString(R.string.stop));
                } else {
                    pB.setVisibility(View.INVISIBLE);
                    btnFindDev.setText(getString(R.string.find));
                    stopFindNewDevice();
                }
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void initSetup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {

            pairedDeviceArrayList = new ArrayList<>();

            for (BluetoothDevice device : pairedDevices) {
                map = new HashMap<>();
                map.put("Name", device.getName());
                map.put("MAC", device.getAddress());
                pairedDeviceArrayList.add(map);
            }

            pairedDeviceAdapter = new SimpleAdapter(this, pairedDeviceArrayList, android.R.layout.simple_list_item_2,
                    new String[] { "Name", "MAC"},
                    new int[] { android.R.id.text1, android.R.id.text2 });
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() { // Клик по нужному устройству

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    HashMap<String, String>  itemValue = (HashMap<String, String>) listViewPairedDevice.getItemAtPosition(position);
                    String name = itemValue.get("Name");
                    String MAC = itemValue.get("MAC");

                    final BluetoothDevice device2 = bluetoothAdapter.getRemoteDevice(MAC);

                    final Context context = ActivityFindDevicePage.this;
                    ad = new AlertDialog.Builder(context);
                    ad.setTitle("Синхронизация");
                    ad.setMessage(String.format("Вы уверены, что хотите установить соединение с устройством %s %s",
                            name, MAC));
                    ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            Toast.makeText(context, "Установка соединения...",
                                    Toast.LENGTH_LONG).show();


                            connectBtThread = new ConnectBtThread(device2);
                            connectBtThread.start();  // Запускаем поток для подключения Bluetooth
                        }
                    });
                    ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            Toast.makeText(context, "Подключение отменено пользователем", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                    ad.show();

                }
            });
        }
    }

    private boolean startfindNewDevice() {
        boolean b = bluetoothAdapter.startDiscovery();

        if (b)
            Toast.makeText(this, "Поиск устройств начат", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Не удалось начать поиск устройств", Toast.LENGTH_SHORT).show();

        return b;
    }

    private void stopFindNewDevice() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private class SingBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String derp = device.getName() + " - " + device.getAddress();
                Toast.makeText(context, derp, Toast.LENGTH_LONG).show();

                map = new HashMap<String, String>();
                map.put("Name", device.getName());
                map.put("MAC", device.getAddress());

                int ind = pairedDeviceArrayList.indexOf(map);
                if (ind == -1) {
                    pairedDeviceArrayList.add(0, map);
                    pairedDeviceAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class ConnectBtThread extends Thread { // Поток для коннекта с Bluetooth

        private BluetoothSocket bluetoothSocket = null;

        private ConnectBtThread(BluetoothDevice device) {

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() { // Коннект
            boolean success = false;

            try {
                bluetoothSocket.connect();
                success = true;
            }

            catch (IOException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ActivityFindDevicePage.this, "Не возможно установить Bluetooth соедненение", Toast.LENGTH_LONG).show();
                    }
                });

                try {
                    bluetoothSocket.close();
                }

                catch (IOException e1) {

                    e1.printStackTrace();
                }
            }

            if(success) {  // Если законнектились, то init device

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // диалог ожидания
                        Toast.makeText(ActivityFindDevicePage.this, "Соединение успешно установлено", Toast.LENGTH_LONG).show();

                    }
                });

                // ============== Начальная синхронизация ==========================================

                initBtThread = new InitBtThread(bluetoothSocket);
                initBtThread.start();
                initBtThread.write("F".getBytes());

                // =================================================================================
                //this.cancel();
            }
        }

        public void cancel() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ActivityFindDevicePage.this, "Closed - Bt Socket", Toast.LENGTH_LONG).show();
                }
            });

            try {
                bluetoothSocket.close();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // END ConnectBtThread:

    private class InitBtThread extends Thread {
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        BluetoothSocket bluetoothSocket;

        private String sbprint;

        public InitBtThread(BluetoothSocket socket) {

            InputStream in = null;
            OutputStream out = null;

            bluetoothSocket = socket;

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
            while (true) {
                try {
                    byte[] buffer = new byte[1];
                    int bytes = connectedInputStream.read(buffer);
                    String strIncom = new String(buffer, 0, bytes);
                    sb.append(strIncom); // собираем символы в строку
                    int endOfLineIndex = sb.indexOf("\r\n"); // определяем конец строки

                    if (endOfLineIndex > 0) {

                        sbprint = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());

                        runOnUiThread(new Runnable() { // Вывод данных

                            @Override
                            public void run() {

                                // sbprint
                            }
                        });
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
            this.cancel();
        }

        public void cancel() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ActivityFindDevicePage.this, "Closed - Bt Socket", Toast.LENGTH_LONG).show();
                }
            });

            try {
                bluetoothSocket.close();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

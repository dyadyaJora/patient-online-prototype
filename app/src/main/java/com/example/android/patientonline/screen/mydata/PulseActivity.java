package com.example.android.patientonline.screen.mydata;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.patientonline.R;
import com.example.android.patientonline.data.DataBaseHelper;
import com.example.android.patientonline.screen.devices.ActivityFindDevicePage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class PulseActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnUp, btnGo;
    public InitBtThread initBtThread;
    UUID myUUID;
    StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);
        setTitle("Данные");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnUp = (Button) findViewById(R.id.btnPulseUp);
        btnUp.setOnClickListener(this);

        btnGo =  (Button) findViewById(R.id.btnPulseGo);
        btnGo.setOnClickListener(this);

        final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnPulseUp:
                break;
            case R.id.btnPulseGo:
                break;
        }
    }

    public void getPulseUp() {

    }

    public void goGetPulse() {

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
                        Toast.makeText(PulseActivity.this, "Не возможно установить Bluetooth соедненение", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(PulseActivity.this, "Соединение успешно установлено", Toast.LENGTH_LONG).show();

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
                    Toast.makeText(PulseActivity.this, "Closed - Bt Socket", Toast.LENGTH_LONG).show();
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

                        String[] arr = sbprint.split(";", -1);
                        String toastText = "";
                        long y = 0;

                        /*if (arr.length > 1) {
                            db = dbHelper.getWritableDatabase();
                            Cursor cur = db.query(DataBaseHelper.TABLE_DEVICES, null, "name = ?",
                                    new String[]{arr[0]}, null, null, null);
                            if (cur.getCount() == 0) {
                                String mac = bluetoothSocket.getRemoteDevice().getAddress();
                                String btName = bluetoothSocket.getRemoteDevice().getName();

                                ContentValues cv = new ContentValues();
                                cv.put(dbHelper.COL_NAME, arr[0]);
                                cv.put(dbHelper.COL_DESCRIPTION, arr[1]);
                                cv.put(dbHelper.COL_TYPE, arr[2]);
                                cv.put(dbHelper.COL_FORMAT, arr[3]);
                                cv.put(dbHelper.COL_BT_NAME, btName);
                                cv.put(dbHelper.COL_MAC, mac);

                                y = db.insert(dbHelper.TABLE_DEVICES, null, cv);
                                toastText = "Новое устройство успешно добавлено";
                            } else {
                                toastText = "Ошибка - устройство уже подключено";
                            }
                            cur.close();
                            db.close();
                        }

                        final String x = toastText;
                        final String fName = arr[0];
                        final long fId = y;88888*/
                        runOnUiThread(new Runnable() { // Вывод данных

                            @Override
                            public void run() {
                                //Toast.makeText(PulseActivity.this, x, Toast.LENGTH_LONG).show();

                                //Intent intent = new Intent();
                                //intent.putExtra("name", fName);
                                //intent.putExtra("id", fId);
                                //setResult(RESULT_OK, intent);
                                //finish();
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
                    Toast.makeText(PulseActivity.this, "Closed - Bt Socket", Toast.LENGTH_LONG).show();
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

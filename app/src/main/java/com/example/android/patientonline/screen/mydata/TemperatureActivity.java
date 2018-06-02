package com.example.android.patientonline.screen.mydata;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.patientonline.R;
import com.example.android.patientonline.data.BtHelper;
import com.example.android.patientonline.data.ConnectingBtThread;
import com.example.android.patientonline.data.DataBaseHelper;
import com.example.android.patientonline.data.RunningBtThread;
import com.example.android.patientonline.service.BtDataRunTempService;
import com.example.android.patientonline.service.BtDataService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class TemperatureActivity extends AppCompatActivity implements View.OnClickListener, BtDataRunTempService.Callback {

    final int CODE_1 = 1;
    Button btnUp, btnGo;
    ProgressDialog pd;
    TextView tempText, tvDis;
    View activePulse;
    Chronometer chPulse;

    BtDataRunTempService service;
    Intent serviceIntent;
    PendingIntent pi;

    Animation anim = null, anim2 = null;

    static long chTime;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(TemperatureActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();

            BtDataRunTempService.LocalBinder binder = (BtDataRunTempService.LocalBinder) iBinder;
            service = (BtDataRunTempService) binder.getServiceInstance();

            // TODO: activity reaction
            btnGo.setText("Стоп");
            btnUp.setEnabled(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(TemperatureActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            BtDataRunTempService.unregisterActivity(TemperatureActivity.this);

            // TODO: activity reaction
            btnGo.setText("Запись");
            btnUp.setEnabled(true);
            animStop();
            chPulse.stop();
            chTime = 0;
            // chPulse.setBase(SystemClock.elapsedRealtime());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        setTitle("Данные");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnUp = (Button) findViewById(R.id.btnTempUp);
        btnUp.setOnClickListener(this);

        btnGo =  (Button) findViewById(R.id.btnTempGo);
        btnGo.setOnClickListener(this);

        tempText = (TextView) findViewById(R.id.tvTempText);
        tvDis = (TextView) findViewById(R.id.tvDisconnect);
        chPulse = (Chronometer) findViewById(R.id.chronometer_temp);
        chPulse.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {
                long t = System.currentTimeMillis() - cArg.getBase();
                cArg.setText(DateFormat.format("mm:ss", t));
            }
        });

        activePulse = findViewById(R.id.view_active_temp);
        anim = AnimationUtils.loadAnimation(this, R.anim.opacity_pulse);
        anim2 = AnimationUtils.loadAnimation(this, R.anim.opacity_pusle_text);

        anim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                activePulse.setVisibility(View.INVISIBLE);
                tvDis.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        pd = new ProgressDialog(this);
        pd.setTitle("Идет подключение");
        pd.setMessage("Ожидайте");
        pd.setCancelable(false);


        pi = createPendingResult(CODE_1, new Intent(), 0);
        serviceIntent = new Intent(TemperatureActivity.this, BtDataRunTempService.class);
        serviceIntent.putExtra("type", "temp");
        serviceIntent.putExtra("pintent", pi);

        if (isMyServiceRunning(BtDataRunTempService.class)) {
            BtDataService.registerActivity(TemperatureActivity.this);
            bindService(serviceIntent, connection, BIND_AUTO_CREATE);
            // btnUp.setEnabled(false);
            // btnGo.setText("Стоп");

            onStartCallback();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BtDataService.unregisterActivity(TemperatureActivity.this);
        try {
            unbindService(connection);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnTempUp:
                getPulseUp();
                break;
            case R.id.btnTempGo:
                if (!isMyServiceRunning(BtDataRunTempService.class)) {
                    goGetPulse();
                } else {
                    stopGetPulse();
                }
                break;
        }
    }

    public void getPulseUp() {
        pd.show();
        if (!BtHelper.checkSocket("temp")) {
            connectBt();
        } else if(!BtHelper.isConnected("temp")) {
            BtHelper.deleteSocket("temp");
            connectBt();
        }
        else {
            startRunning();
        }
    }

    public void goGetPulse() {
        BtDataRunTempService.registerActivity(TemperatureActivity.this);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        pd.show();
    }

    public void stopGetPulse() {
        BtDataRunTempService.unregisterActivity(TemperatureActivity.this);
        unbindService(connection);
        stopService(serviceIntent);

        btnGo.setText("Запись");
        btnUp.setEnabled(true);
        pd.cancel();
        animStop();
        chPulse.stop();
        chTime = 0;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStartCallback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                animRun();
                pd.cancel();

                if (chTime == 0)
                    chTime = SystemClock.elapsedRealtime();

                chPulse.setVisibility(View.VISIBLE);
                chPulse.setBase(BtDataRunTempService.whenTime);
                chPulse.start();
            }
        });
    }

    @Override
    public void onTickCallback(HashMap data) {
        final double temp = (double) data.get("main_val");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempText.setText(String.valueOf(temp));
            }
        });
        // TODO: ======================================
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BtDataRunTempService.STATUS_FINISH) {

            btnGo.setText("Запись");
            btnUp.setEnabled(true);
            pd.cancel();
            // TODO: обработка завершения записи
            try {
                unbindService(connection);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            stopService(serviceIntent);
            animStop();
            chPulse.stop();
            chTime = 0;
            // chPulse.setBase(SystemClock.elapsedRealtime());
        }
    }

    private void animRun() {
        activePulse.setVisibility(View.VISIBLE);
        activePulse.startAnimation(anim);
    }

    private void animStop() {
        activePulse.setVisibility(View.INVISIBLE);
        activePulse.clearAnimation();
    }

    private void connectBt() {
        ConnectBtCallback cb = new ConnectBtCallback();
        String MAC = "00:0D:47:54:5D:93"; // TODO: remove hardcode to dbHelper
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MAC);

        ConnectingBtThread thr1 = new ConnectingBtThread(device, "temp", TemperatureActivity.this, cb);
        thr1.start();
    }

    private void startRunning() {
        RunBtCallback cb = new RunBtCallback();

        RunningBtThread thr2 = new RunningBtThread("temp", TemperatureActivity.this, cb);
        thr2.start();
        thr2.write("G".getBytes());
    }

    private class ConnectBtCallback implements ConnectingBtThread.Callback {

        @Override
        public void cb(String type) {

            startRunning();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.cancel();
                }
            });
        }

        @Override
        public void closeCb() {
            // pass
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.cancel();
                }
            });
        }
    }


    private class RunBtCallback implements RunningBtThread.Callback {

        @Override
        public void dataProcessingCb(RunningBtThread thr, InputStream in) {

            DataBaseHelper dbHelper;
            SQLiteDatabase db;
            StringBuilder sb = new StringBuilder();
            String sbprint;

            while (true) {
                try {
                    byte[] buffer = new byte[1];
                    int bytes = in.read(buffer);
                    String strIncom = new String(buffer, 0, bytes);
                    sb.append(strIncom); // собираем символы в строку
                    int endOfLineIndex = sb.indexOf("\r\n"); // определяем конец строки


                    if (endOfLineIndex > 0) {

                        sbprint = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());

                        String [] arr = sbprint.split(";", -1);

                        double tempVal = Double.parseDouble(sbprint);

                        HashMap<String, Object> map = new HashMap();
                        map.put("main_val", tempVal);
                        map.put("type", 2);

                        onTickCallback(map);
                        // TODO: SAVE to db
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
            thr.cancel();
        }

        @Override
        public void closeCb() {
            // pass
        }
    }
}

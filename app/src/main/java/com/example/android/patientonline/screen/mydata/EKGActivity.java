package com.example.android.patientonline.screen.mydata;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.android.patientonline.R;
import com.example.android.patientonline.data.ChartHelper;
import com.example.android.patientonline.service.BtDataRunPulseService;
import com.github.mikephil.charting.charts.LineChart;

import java.util.HashMap;

public class EKGActivity extends AppCompatActivity implements BtDataRunPulseService.Callback, View.OnClickListener {

    ChartHelper chart;
    BtDataRunPulseService service;
    Intent serviceIntent;

    Button btnGo;
    Chronometer chPulse;
    ProgressDialog pd;

    static long chTime = 0;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(EKGActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();

            BtDataRunPulseService.LocalBinder binder = (BtDataRunPulseService.LocalBinder) iBinder;
            service = (BtDataRunPulseService) binder.getServiceInstance();

            // TODO: activity reaction
            btnGo.setText("Стоп");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(EKGActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            BtDataRunPulseService.unregisterActivity(EKGActivity.this);

            // TODO: activity reaction
            btnGo.setText("Запись");
            chPulse.stop();
            chTime = 0;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ekg);
        setTitle("Данные");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnGo = (Button) findViewById(R.id.btnPulseGo);
        btnGo.setOnClickListener(this);
        chPulse = (Chronometer) findViewById(R.id.chronometer_pulse);
        chPulse.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {
                long t = System.currentTimeMillis() - cArg.getBase();
                cArg.setText(DateFormat.format("mm:ss", t));
            }
        });

        pd = new ProgressDialog(this);
        pd.setTitle("Идет подключение");
        pd.setMessage("Ожидайте");
        pd.setCancelable(false);

        chart = new ChartHelper((LineChart) findViewById(R.id.chart), this);
        chart.initialize();

        serviceIntent = new Intent(EKGActivity.this, BtDataRunPulseService.class);

        if (isMyServiceRunning(BtDataRunPulseService.class)) {
            BtDataRunPulseService.registerActivity(EKGActivity.this);
            bindService(serviceIntent, connection, BIND_AUTO_CREATE);

            onStartCallback();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BtDataRunPulseService.unregisterActivity(EKGActivity.this);
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
    public void onStartCallback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chart.clearChart();
                pd.cancel();

                if (chTime == 0)
                    chTime = SystemClock.elapsedRealtime();

                chPulse.setVisibility(View.VISIBLE);
                chPulse.setBase(BtDataRunPulseService.whenTime);
                chPulse.start();
            }
        });
    }

    @Override
    public void onTickCallback(HashMap data) {
        final float analog = Float.parseFloat(data.get("analog").toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chart.addEntry(analog);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch ((view.getId())) {
            case R.id.btnPulseGo:
                if (!isMyServiceRunning(BtDataRunPulseService.class)) {
                    goGetPulse();
                } else {
                    stopGetPulse();
                }
                break;
        }
    }

    private void goGetPulse() {
        BtDataRunPulseService.registerActivity(EKGActivity.this);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        pd.show();
    }

    private void stopGetPulse() {
        BtDataRunPulseService.unregisterActivity(EKGActivity.this);
        unbindService(connection);
        stopService(serviceIntent);

        btnGo.setText("Запись");
        pd.cancel();
        chPulse.stop();
        chTime = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BtDataRunPulseService.STATUS_FINISH) {

            btnGo.setText("Запись");
            pd.cancel();

            try {
                unbindService(connection);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            stopService(serviceIntent);
            chPulse.stop();
            chTime = 0;
        }
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
}

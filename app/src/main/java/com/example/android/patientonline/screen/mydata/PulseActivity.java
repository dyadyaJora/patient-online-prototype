package com.example.android.patientonline.screen.mydata;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.patientonline.R;
import com.example.android.patientonline.service.BtDataRunPulseService;

public class PulseActivity extends AppCompatActivity implements View.OnClickListener, BtDataRunPulseService.Callback {

    final int CODE_1 = 1;
    Button btnUp, btnGo;
    ProgressDialog pd;
    BtDataRunPulseService service;
    Intent serviceIntent;
    PendingIntent pi;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(PulseActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();

            BtDataRunPulseService.LocalBinder binder = (BtDataRunPulseService.LocalBinder) iBinder;
            service = (BtDataRunPulseService) binder.getServiceInstance();

            // TODO: activity reaction
            btnGo.setText("Стоп");
            btnUp.setEnabled(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(PulseActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
            BtDataRunPulseService.unregisterActivity(PulseActivity.this);

            // TODO: activity reaction
            btnGo.setText("Запись");
            btnUp.setEnabled(true);
        }
    };


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

        pd = new ProgressDialog(this);
        pd.setTitle("Идет подключение");
        pd.setMessage("Ожидайте");
        pd.setCancelable(false);

        if (isMyServiceRunning(BtDataRunPulseService.class)) {
            btnUp.setEnabled(false);
            btnGo.setText("Стоп");
        }


        pi = createPendingResult(CODE_1, new Intent(), 0);
        serviceIntent = new Intent(PulseActivity.this, BtDataRunPulseService.class);
        serviceIntent.putExtra("type", "pulse");
        serviceIntent.putExtra("pintent", pi);
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
                if (!isMyServiceRunning(BtDataRunPulseService.class)) {
                    goGetPulse();
                } else {
                    stopGetPulse();
                }
                break;
        }
    }

    public void getPulseUp() {

    }

    public void goGetPulse() {
        BtDataRunPulseService.registerActivity(PulseActivity.this);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        pd.show();
    }

    public void stopGetPulse() {
        unbindService(connection);
        stopService(serviceIntent);

        btnGo.setText("Запись");
        btnUp.setEnabled(true);
        pd.cancel();
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
        // ?
        pd.cancel();
    }

    @Override
    public void onTickCallback() {
        // TODO: ======================================
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BtDataRunPulseService.STATUS_FINISH) {

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
        }
    }
}

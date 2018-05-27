package com.example.android.patientonline.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.android.patientonline.R;
import com.example.android.patientonline.data.BtHelper;
import com.example.android.patientonline.data.ConnectingBtThread;
import com.example.android.patientonline.data.RunningBtThread;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class BtDataService extends Service {
    static ArrayList<Callback> activityList = new ArrayList<>();

    private final IBinder lB = new LocalBinder();
    public int NOTIFICATION_ID;
    public String NOTIFICATION_TEXT, NOTIFICATION_TITLE;
    private String type;
    private static int startId;
    public static final int STATUS_FINISH = 200;

    ConnectingBtThread thr1;
    RunningBtThread thr2;
    Handler handler = new Handler();
    PendingIntent pi;

    @Override
    public IBinder onBind(Intent intent) {
        return lB;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        type = intent.getStringExtra("type");
        pi = intent.getParcelableExtra("pintent");
        BtDataService.startId = startId;

        if (!BtHelper.checkSocket(type)) {
            connectBt();
        } else if(!BtHelper.isConnected(type)) {
            BtHelper.deleteSocket(type);
            connectBt();
        }
        else {
            startRunning();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            thr1.cancel();
            thr1.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            thr2.write("S".getBytes());
            thr2.cancel();
            thr2.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static void registerActivity(Activity a) {
        activityList.add( (Callback) a);
    }

    public static void unregisterActivity(Activity a) {
        activityList.remove(a);
    }

    private void connectBt() {
        ConnectBtCallback cb = new ConnectBtCallback();
        String MAC = "00:0D:47:54:5D:93"; // TODO: remove hardcode to dbHelper
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MAC);

        thr1 = new ConnectingBtThread(device, type, (Activity) activityList.get(0), cb);
        thr1.start();
    }

    private void startRunning() {
        RunBtCallback cb = new RunBtCallback();
        thr2 = new RunningBtThread(type, (Activity) activityList.get(0), cb);
        thr2.start();
        thr2.write("R".getBytes());
    }

    public abstract void dataProcessing();

    private void updateActivities(HashMap data) {
        // update notification

        for (Callback activity : activityList) {
            activity.onTickCallback();
        }
    }

    private class ConnectBtCallback implements ConnectingBtThread.Callback {

        @Override
        public void cb(String type) {
            startRunning();
        }

        @Override
        public void closeCb() {
            try {
                pi.send(STATUS_FINISH);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            stopSelf(startId);
        }
    }


    private class RunBtCallback implements RunningBtThread.Callback {

        @Override
        public void dataProcessingCb(RunningBtThread thr, InputStream in) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setUsesChronometer(true)
                            .setContentTitle(NOTIFICATION_TITLE)
                            .setContentText(NOTIFICATION_TEXT);

            Notification notification = builder.build();

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);

            // ====== зачем? ============
            for (Callback a: activityList) {
                a.onStartCallback();
            }
            // ==========================

            // ==================================================================
            dataProcessing();

        }

        @Override
        public void closeCb() {
            try {
                pi.send(STATUS_FINISH);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            stopSelf(startId);
        }
    }

    public class LocalBinder extends Binder {
        public BtDataService getServiceInstance(){
            return BtDataService.this;
        }
    }

    public interface Callback {
        void onStartCallback();
        void onTickCallback();
    }
}

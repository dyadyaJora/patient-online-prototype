package com.example.android.patientonline.service;

import java.io.InputStream;

public class BtDataRunTempService extends BtDataService {
    public int NOTIFICATION_ID = 1;
    public String NOTIFICATION_TEXT, NOTIFICATION_TITLE;

    @Override
    public void dataProcessing(InputStream in) {
        // while (1) { do something ; updateActivities();}

    }

    @Override
    public String updateNotificationText(int pulse) {
        return null;
    }
}

package com.example.android.patientonline.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class BtDataRunTempService extends BtDataService {

    public BtDataRunTempService() {

        NOTIFICATION_TEXT = "Значение температуры 36.6 °C";
        NOTIFICATION_TITLE = "Идёт измерение температуры";
        NOTIFICATION_ID = 2;
    }

    @Override
    public void dataProcessing(InputStream in) {
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

                    double tempVal = Double.parseDouble(sbprint);

                    HashMap<String, Object> map = new HashMap();
                    map.put("main_val", tempVal);
                    map.put("type", 2);

                    updateActivities(map);
                }
            } catch (IOException e) {
                stopSelf(startId);
                // TODO обработка ошибок, kill thread, stopService
                break;
            }
        }

    }

    @Override
    public String updateNotificationText(String temp) {
        return "Значение температуры " + temp + " °C";
    }
}

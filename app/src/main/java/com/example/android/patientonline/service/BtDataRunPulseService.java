package com.example.android.patientonline.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class BtDataRunPulseService extends BtDataService {

    public BtDataRunPulseService() {
        NOTIFICATION_TEXT = "Значение пульса 60 уд/мин";
        NOTIFICATION_TITLE = "Идёт измерение пульса";
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

                    String[] arr = sbprint.split(";", -1);

                    if (arr.length == 4) {
                        int pulseVal = Integer.parseInt(arr[0]);
                        int valid = Integer.parseInt(arr[1]);
                        int beep = Integer.parseInt(arr[2]);
                        int analogVal = Integer.parseInt(arr[3]);

                        if (valid == 1) {
                            if (pulseVal > 150)
                                pulseVal = 150;

                            if (pulseVal < 50)
                                pulseVal = 50;

                            HashMap<String, Integer> map = new HashMap();
                            map.put("main_val", pulseVal);
                            map.put("valid", valid);
                            map.put("beep", beep);
                            map.put("analog", analogVal);

                            updateActivities(map);
                        } else {

                        }
                    }
                }
            } catch (IOException e) {
                stopSelf(startId);
                // TODO обработка ошибок, kill thread, stopService
                break;
            }
        }
    }

    @Override
    public String updateNotificationText(String pulse) {
        return "Значение пулса " + pulse + " уд/мин";
    }
}

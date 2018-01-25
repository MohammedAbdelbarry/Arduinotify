package com.example.notify.arduino.androidnotificationlistener;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by mohammed on 1/23/18.
 */

public class ArduinoListenerService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();

    protected static final String NOTIFICATION_POSTED_ACTION =
            "com.example.notify.arduino.androidnotificationlistener" +
                    ".NOTIFICATION_LISTENER_POSTED_ACTION";

    protected static final String NOTIFICATION_REMOVED_ACTION =
            "com.example.notify.arduino.androidnotificationlistener" +
                    ".NOTIFICATION_LISTENER_REMOVED_ACTION";

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String getTickerText(Notification notification) {
        final String TITLE_KEY = "android.title";
        final String TEXT_KEY = "android.text";
        String ticker = "";

        if (notification != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Bundle extras = notification.extras;
                    String extraTitle = extras.getString(TITLE_KEY);
                    String extraText = null;
                    Object extraTextExtra = extras.get(TEXT_KEY);
                    if (extraTextExtra != null) extraText = extraTextExtra.toString();

                    if (extraTitle != null && extraText != null && !extraText.isEmpty()) {
                        ticker = extraTitle + ": " + extraText;
                    } else if (extraTitle != null) {
                        ticker = extraTitle;
                    } else if (extraText != null) {
                        ticker = extraText;
                    }
                } catch (Exception e) {
                    Log.w("NotificationPlugin", "problem parsing notification extras for " + notification.tickerText);
                    e.printStackTrace();
                }
            }

            if (ticker.isEmpty()) {
                ticker = (notification.tickerText != null) ? notification.tickerText.toString() : "";
            }
        }

        return ticker;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG,"**********onNotificationPosted**********");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + getTickerText(sbn.getNotification())
                + "\t" + sbn.getPackageName());

        Intent intent = new Intent(NOTIFICATION_POSTED_ACTION);
        intent.putExtra("package_name", sbn.getPackageName());
        intent.putExtra("notification_text", getTickerText(sbn.getNotification()));
        //intent.putExtra("notification_icon", sbn.getNotification().getSmallIcon());
        //intent.putExtra("status_bar_notification", sbn);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"**********onNotificationRemoved**********");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + getTickerText(sbn.getNotification())
                +"\t" + sbn.getPackageName());

        Intent intent = new Intent(NOTIFICATION_REMOVED_ACTION);
        intent.putExtra("package_name", sbn.getPackageName());
        intent.putExtra("notification_text", getTickerText(sbn.getNotification()));
        //intent.putExtra("notification_icon", sbn.getNotification().getSmallIcon());
//        intent.putExtra("status_bar_notification", sbn);
        sendBroadcast(intent);
    }

}

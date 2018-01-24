package com.example.notify.arduino.androidnotificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();

    /**
     * A TextView for testing.
     */
    private TextView txtView;

    private NotificationReceiver notificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        notificationReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoListenerService.NOTIFICATION_POSTED_ACTION);
        filter.addAction(ArduinoListenerService.NOTIFICATION_REMOVED_ACTION);
        registerReceiver(notificationReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
//        stopService(new Intent(this, ArduinoListenerService.class));
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ArduinoListenerService.NOTIFICATION_POSTED_ACTION:
                    handleNotificationPosted(context, intent);
                    break;
                case ArduinoListenerService.NOTIFICATION_REMOVED_ACTION:
                    handleNotificationRemoved(context, intent);
                    break;
                default:
                    break;
            }
        }

        private void handleNotificationPosted(Context context, Intent intent) {
            Log.i(TAG, "**********handleNotificationPosted**********");
            Log.i(TAG, "Posted: " + intent.getStringExtra("package_name")
                    + ": " + intent.getStringExtra("notification_text"));

            // TODO: use the sbn to get the icon
//            StatusBarNotification sbn = intent.getParcelableExtra("status_bar_notification");

            txtView.setText(txtView.getText() + "\n" + "Posted: "
                    + intent.getStringExtra("package_name") + ": "
                    + intent.getStringExtra("notification_text"));
        }

        private void handleNotificationRemoved(Context context, Intent intent) {
            Log.i(TAG, "**********handleNotificationRemoved**********");
            Log.i(TAG, "Removed: " + intent.getStringExtra("package_name") + ": "
                    + intent.getStringExtra("notification_text"));

            // TODO: use the sbn to get the icon
//            StatusBarNotification sbn = intent.getParcelableExtra("status_bar_notification");

            txtView.setText(txtView.getText() + "\n" + "Removed: "
                    + intent.getStringExtra("package_name") + ": "
                    + intent.getStringExtra("notification_text"));
        }
    }
}

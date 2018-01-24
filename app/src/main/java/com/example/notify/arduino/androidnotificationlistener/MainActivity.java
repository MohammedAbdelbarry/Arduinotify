package com.example.notify.arduino.androidnotificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notify.arduino.bluetoothcontrol.BluetoothConnection;

public class MainActivity extends AppCompatActivity {

    /* HC-05 Bluetooth module MAC address. */
    protected static final String HC05_MAC_ADDRESS = "98:D3:32:10:E9:4D";

    private String TAG = this.getClass().getSimpleName();
    /**
     * A TextView for testing.
     */
    private TextView txtView;

    private Button connectButton;

    private NotificationReceiver notificationReceiver;

    private BluetoothConnection btConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView) findViewById(R.id.textView);
        connectButton = (Button) findViewById(R.id.connectButton);
        notificationReceiver = new NotificationReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoListenerService.NOTIFICATION_POSTED_ACTION);
        filter.addAction(ArduinoListenerService.NOTIFICATION_REMOVED_ACTION);
        registerReceiver(notificationReceiver, filter);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btConnection = new BluetoothConnection(HC05_MAC_ADDRESS);
                btConnection.execute();
            }
        });
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

            if (btConnection != null) {
                Log.i(TAG, "Sent '1' via bluetooth.");
                btConnection.send("1");
            } else {
                Toast.makeText(getApplicationContext(), "Connect to HC-05 first.",
                        Toast.LENGTH_LONG);
            }
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

            if (btConnection != null) {
                Log.i(TAG, "Sent '0' via bluetooth.");
                btConnection.send("0");
            } else {
                Toast.makeText(getApplicationContext(), "Connect to HC-05 first.",
                        Toast.LENGTH_LONG);
            }
        }
    }
}

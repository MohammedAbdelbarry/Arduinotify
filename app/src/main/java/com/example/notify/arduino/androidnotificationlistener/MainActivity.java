package com.example.notify.arduino.androidnotificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notify.arduino.bluetoothcontrol.BluetoothConnection;

import java.util.HashMap;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    /* HC-05 Bluetooth module MAC address. */
    protected static final String HC05_MAC_ADDRESS = "98:D3:32:10:E9:4D";

    private static final String APP_COLORS_PREF = "AppColors";

    private String TAG = this.getClass().getSimpleName();
    /**
     * A TextView for testing.
     */
    private TextView txtView;

    private Button connectButton;

    private Button configButton;

    private NotificationReceiver notificationReceiver;

    private ColorReceiver colorReceiver;

    private BluetoothConnection btConnection;

    private HashMap<String, Integer> appColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView) findViewById(R.id.textView);
        connectButton = (Button) findViewById(R.id.connectButton);
        configButton = (Button) findViewById(R.id.configButton);
        notificationReceiver = new NotificationReceiver();
        colorReceiver = new ColorReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoListenerService.NOTIFICATION_POSTED_ACTION);
        filter.addAction(ArduinoListenerService.NOTIFICATION_REMOVED_ACTION);
        registerReceiver(notificationReceiver, filter);

        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(ApplicationColorActivity.APP_SELECTED_ACTION);
        registerReceiver(colorReceiver, appFilter);

        SharedPreferences prefs = getSharedPreferences(APP_COLORS_PREF, Context.MODE_PRIVATE);
        appColors = new HashMap<>();
        HashMap<String, Integer> prefsMap = (HashMap<String, Integer>) prefs.getAll();
        for (String packageName : prefsMap.keySet()) {
            appColors.put(packageName, prefsMap.get(packageName));
        }
        Log.i(TAG, "Number of items in appColors: " + appColors.size());
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btConnection = new BluetoothConnection(HC05_MAC_ADDRESS);
                    btConnection.execute();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to Connect to HC-05", Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveMap(HashMap<String, Integer> map, String sharedPrefName) {
        Log.i(TAG, "**********saveMap**********");
        Log.i(TAG, "Number of items in map: " + map.size());
        SharedPreferences prefs = getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String packageName : map.keySet()) {
            editor.putInt(packageName, map.get(packageName));
        }
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
        unregisterReceiver(colorReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMap(appColors, APP_COLORS_PREF);
        stopService(new Intent(this, ArduinoListenerService.class));
    }

    public void configClicked(View view) {
        Log.i(TAG, "**********configClicked**********");
        Intent intent = new Intent(this, ApplicationColorActivity.class);
        intent.putExtra("selected_apps", appColors);
        startActivity(intent);
    }

    class ColorReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "**********ColorReceiver**********");
            Log.i(TAG, "**********onReceive**********");
            String packageName = intent.getStringExtra("package_name");
            int color = intent.getIntExtra("color", -1);
            if (color == -1 || packageName == null)
                return;
            Log.i(TAG, "package: " + packageName + "\tColor: " + Integer.toHexString(color));
            appColors.put(packageName, color);
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        private String getAppName(String packageName) {
            final PackageManager pm = getApplicationContext().getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(packageName, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            return (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);
        }

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

            String packageName = intent.getStringExtra("package_name");

            txtView.setText(txtView.getText() + "\n" + "Posted: "
                    + packageName + ": "
                    + intent.getStringExtra("notification_text"));


            int color;
            if (appColors.containsKey(packageName)) {
                color = appColors.get(packageName);
                Log.i(TAG, "Found Package: " + packageName + "\tColor: " + Integer.toHexString(color));
            } else {
                return;
            }

            if (btConnection != null) {
                Log.i(TAG, "Sent 'Notification' via bluetooth.");

                String colorString = "" + Color.red(color) + ";" + Color.green(color)
                        + ";" + Color.blue(color);
                String msg = "post;" + packageName + ";" + colorString;
                btConnection.send(msg);
            } else {
                Toast.makeText(getApplicationContext(), "Connect to HC-05 first.",
                        Toast.LENGTH_LONG);
            }
        }

        private void handleNotificationRemoved(Context context, Intent intent) {
            Log.i(TAG, "**********handleNotificationRemoved**********");
            Log.i(TAG, "Removed: " + intent.getStringExtra("package_name") + ": "
                    + intent.getStringExtra("notification_text"));

            String packageName = intent.getStringExtra("package_name");

            txtView.setText(txtView.getText() + "\n" + "Removed: "
                    + packageName + ": "
                    + intent.getStringExtra("notification_text"));

            if (btConnection != null) {
                Log.i(TAG, "Sent 'Removal' via bluetooth.");
                btConnection.send("rem;" + packageName);
            } else {
                Toast.makeText(getApplicationContext(), "Connect to HC-05 first.",
                        Toast.LENGTH_LONG);
            }
        }
    }
}

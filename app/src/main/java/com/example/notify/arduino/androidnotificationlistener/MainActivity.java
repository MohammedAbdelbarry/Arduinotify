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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notify.arduino.bluetoothcontrol.BluetoothConnection;
import com.example.notify.arduino.listadapters.AppConfigAdapter;
import com.example.notify.arduino.listadapters.ApplicationAdapter;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener {

    /* HC-05 Bluetooth module MAC address. */
    protected static final String HC05_MAC_ADDRESS = "98:D3:32:10:E9:4D";

    private static final String APP_COLORS_PREF = "AppColors";

    private String TAG = this.getClass().getSimpleName();

    private NotificationReceiver notificationReceiver;

//    private ColorReceiver colorReceiver;

    private BluetoothConnection btConnection;

    private HashMap<String, Integer> appColors;


    private static final int COLOR_REQUEST_CODE = 1;

    private List<AppConfig> applist = null;

    private AppConfigAdapter listAdapter = null;

    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.configList);

        notificationReceiver = new NotificationReceiver();
//        colorReceiver = new ColorReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoListenerService.NOTIFICATION_POSTED_ACTION);
        filter.addAction(ArduinoListenerService.NOTIFICATION_REMOVED_ACTION);
        registerReceiver(notificationReceiver, filter);

//        IntentFilter appFilter = new IntentFilter();
//        appFilter.addAction(ApplicationColorActivity.APP_SELECTED_ACTION);
//        registerReceiver(colorReceiver, appFilter);

        SharedPreferences prefs = getSharedPreferences(APP_COLORS_PREF, Context.MODE_PRIVATE);
        appColors = new HashMap<>();
        HashMap<String, Integer> prefsMap = (HashMap<String, Integer>) prefs.getAll();
        for (String packageName : prefsMap.keySet()) {
            appColors.put(packageName, prefsMap.get(packageName));
        }


        applist = new ArrayList<>();

        if (appColors != null) {
            PackageManager pm = getPackageManager();
            if (pm != null) {
                for (String packageName : appColors.keySet()) {
                    try {
                        ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                        int color = appColors.get(packageName);
                        applist.add(new AppConfig(info, color));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        listAdapter = new AppConfigAdapter(this,
                R.layout.app_select_list_row, applist);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    ColorPickerDialog.newBuilder().setColor(Color.BLUE)
                            .setDialogId(position).show(MainActivity.this);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        Log.i(TAG, "Number of items in appColors: " + appColors.size());
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

    public void fabClicked(View view) {
        Log.i(TAG, "**********fabClicked**********");
        Intent intent = new Intent(this, AllAppsActivity.class);
        startActivityForResult(intent, COLOR_REQUEST_CODE);
    }

    private void insert(List<AppConfig> list, @NonNull ApplicationInfo info, @ColorInt int color) {
        if (info == null)
            return;
        int i = 0;
        for (AppConfig config : list) {
            if (config.applicationInfo != null &&
                    config.applicationInfo.packageName.equals(info.packageName)) {
                listAdapter.remove(config);
                listAdapter.insert(new AppConfig(info, color), i);
                return;
            }
            i++;
        }
        listAdapter.add(new AppConfig(info, color));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ApplicationInfo info = data.getParcelableExtra("app_info");
                int color = data.getIntExtra("color", -1);
                if (info == null || color == -1)
                    return;
                appColors.put(info.packageName, color);
                insert(applist, info, color);
            }
        }
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        AppConfig config = applist.get(dialogId);
        ApplicationInfo info = config.applicationInfo;
        listAdapter.remove(config);
        listAdapter.insert(new AppConfig(info, color), dialogId);
        appColors.put(config.applicationInfo.packageName, color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
//        unregisterReceiver(colorReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMap(appColors, APP_COLORS_PREF);
        stopService(new Intent(this, ArduinoListenerService.class));
    }

    public void configClicked(MenuItem item) {
        Log.i(TAG, "**********configClicked**********");
//        Intent intent = new Intent(this, ApplicationColorActivity.class);
//        intent.putExtra("selected_apps", appColors);
//        startActivity(intent);
    }

    public void connectClicked(MenuItem item) {
        try {
            btConnection = new BluetoothConnection(HC05_MAC_ADDRESS);
            btConnection.execute();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to Connect to HC-05", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

//    class ColorReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "**********ColorReceiver**********");
//            Log.i(TAG, "**********onReceive**********");
//            String packageName = intent.getStringExtra("package_name");
//            int color = intent.getIntExtra("color", -1);
//            if (color == -1 || packageName == null)
//                return;
//            Log.i(TAG, "package: " + packageName + "\tColor: " + Integer.toHexString(color));
//            appColors.put(packageName, color);
//        }
//    }

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

//            txtView.setText(txtView.getText() + "\n" + "Posted: "
//                    + packageName + ": "
//                    + intent.getStringExtra("notification_text"));


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

//            txtView.setText(txtView.getText() + "\n" + "Removed: "
//                    + packageName + ": "
//                    + intent.getStringExtra("notification_text"));

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

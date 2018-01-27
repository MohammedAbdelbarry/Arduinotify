package com.example.notify.arduino.androidnotificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener
        , NavigationView.OnNavigationItemSelectedListener {

    /* HC-05 Bluetooth module MAC address. */
    protected static final String HC05_MAC_ADDRESS = "98:D3:32:10:E9:4D";

    private static final String APP_COLORS_PREF = "AppColors";

    private String TAG = this.getClass().getSimpleName();

    private NotificationReceiver notificationReceiver;

    private BluetoothConnectionReceiver bluetoothConnectionReceiver;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = (ListView) findViewById(R.id.configList);

        notificationReceiver = new NotificationReceiver();
        bluetoothConnectionReceiver = new BluetoothConnectionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoListenerService.NOTIFICATION_POSTED_ACTION);
        filter.addAction(ArduinoListenerService.NOTIFICATION_REMOVED_ACTION);
        registerReceiver(notificationReceiver, filter);

        IntentFilter bluetoothConnectionFilter = new IntentFilter();
        bluetoothConnectionFilter.addAction(BluetoothConnection.BLUETOOTH_CONNECTED_ACTION);
        registerReceiver(bluetoothConnectionReceiver, bluetoothConnectionFilter);


        SharedPreferences prefs = getSharedPreferences(APP_COLORS_PREF, Context.MODE_PRIVATE);
        appColors = new HashMap<>();
        HashMap<String, Integer> prefsMap = (HashMap<String, Integer>) prefs.getAll();
        for (String packageName : prefsMap.keySet()) {
            appColors.put(packageName, prefsMap.get(packageName));
        }


        applist = new ArrayList<>();

        final PackageManager pm = getPackageManager();
        if (appColors != null) {
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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final AppConfig config = applist.get(position);
                final ApplicationInfo info = config.applicationInfo;
                String appName = info.loadLabel(pm).toString();


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog dialog = dialogBuilder.setTitle("Delete \"" + appName + "\"")
                        .setMessage("Are you sure tou want to delete \"" + appName + "\"?")
                        .setCancelable(true)
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.i(TAG, "Clicked Yes");
                                Log.i(TAG, "Size Before: " + appColors.size());
                                Log.i(TAG, "Deletion Status: " + appColors.remove(info.packageName));
                                Log.i(TAG, "Size After: " + appColors.size());
                                listAdapter.remove(config);
                            }
                        }).setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.i(TAG, "Clicked No");
                            }
                        }).create();
                dialog.show();

                return true;
            }
        });

        Log.i(TAG, "Number of items in appColors: " + appColors.size());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveMap(HashMap<String, Integer> map, String sharedPrefName) {
        Log.i(TAG, "**********saveMap**********");
        Log.i(TAG, "Number of items in map: " + map.size());
        SharedPreferences prefs = getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
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
        unregisterReceiver(bluetoothConnectionReceiver);
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
            btConnection = new BluetoothConnection(this, HC05_MAC_ADDRESS);
            btConnection.execute();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to Connect to HC-05", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    class BluetoothConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "**********BluetoothConnectionReceiver**********");
            Log.i(TAG, "**********onReceive**********");
            boolean success = intent.getBooleanExtra("success", false);
            if (success) {
                Date now = new Date();
                DateFormat timeFormat = new SimpleDateFormat("date;dd;MM;yyyy;clock;HH;mm;ss;");
                String formattedTime = timeFormat.format(now);
                btConnection.send(formattedTime);
                sendTemprature();
            } else {
                btConnection = null;
            }
        }
        private void sendTemprature() {
            WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
            WeatherConfig config = new WeatherConfig();
//            config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;
//            config.maxResult = 8;
//            config.numDays = 1;
//            config.lang = "en";
            config.ApiKey = "4af0ec4188ddd3d667cf69bdd7dd9ecc";
            try {
                WeatherClient client = builder.attach(MainActivity.this)
                        .provider(new OpenweathermapProviderType())
                        .httpClient(WeatherDefaultClient.class)
                        .config(config)
                        .build();
                client.getCurrentCondition(new WeatherRequest(31.208180f, 29.924492f), new WeatherClient.WeatherEventListener() {
                    @Override
                    public void onWeatherRetrieved(CurrentWeather currentWeather) {

                        float currentTemp = currentWeather.weather.temperature.getTemp();
                        Log.i(TAG, "City ["+currentWeather.weather.location.getCity()+"] Current temp ["+currentTemp+"C]");
                        int temp = (int) (currentTemp + 0.5f);
                        btConnection.send("temp;" + temp + ";");
                    }

                    @Override
                    public void onWeatherError(WeatherLibException e) {
                        Log.i(TAG, "Weather Error - parsing data");
                        e.printStackTrace();
                    }

                    @Override
                    public void onConnectionError(Throwable throwable) {
                        Log.i(TAG, "Connection error");
                        throwable.printStackTrace();
                    }
                });
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
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

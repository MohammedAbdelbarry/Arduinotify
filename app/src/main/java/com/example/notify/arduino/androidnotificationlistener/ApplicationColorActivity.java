package com.example.notify.arduino.androidnotificationlistener;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ApplicationColorActivity extends AppCompatActivity implements ColorPickerDialogListener {
    private String TAG = this.getClass().getSimpleName();
    private static final int COLOR_REQUEST_CODE = 1;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listAdapter = null;
    private ListView listView;
    protected static final String APP_SELECTED_ACTION = "com.example.notify.arduino" +
            ".androidnotificationlistener.APP_SELECTED_ACTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_color);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.configList);

        HashMap<String, Integer> appColors
                = (HashMap<String, Integer>) getIntent().getSerializableExtra("selected_apps");

        applist = new ArrayList<>();

        if (appColors != null) {
            PackageManager pm = getPackageManager();
            if (pm != null) {
                for (String packageName : appColors.keySet()) {
                    try {
                        applist.add(pm.getApplicationInfo(packageName, 0));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        listAdapter = new ApplicationAdapter(this,
                R.layout.app_select_list_row, applist);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    ColorPickerDialog.newBuilder().setColor(Color.BLUE)
                            .setDialogId(position).show(ApplicationColorActivity.this);
                } catch (Exception e) {
                    Toast.makeText(ApplicationColorActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void fabClicked(View view) {
        Log.i(TAG, "**********fabClicked**********");
        Intent intent = new Intent(this, AllAppsActivity.class);
        startActivityForResult(intent, COLOR_REQUEST_CODE);
    }

    private void broadcastColorChange(ApplicationInfo info, @ColorInt int color) {
        Intent intent = new Intent(APP_SELECTED_ACTION);
        intent.putExtra("package_name", info.packageName);
        intent.putExtra("color", color);
        sendBroadcast(intent);
    }

    private boolean contains(List<ApplicationInfo> list, @NonNull ApplicationInfo info) {
        if (info == null)
            return false;
        for (ApplicationInfo otherInfo : list) {
            if (otherInfo != null && otherInfo.packageName.equals(info.packageName))
                return true;
        }
        return false;
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
                broadcastColorChange(info, color);
                if (!contains(applist, info)) {
                    listAdapter.add(info);
                }
            }
        }
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        ApplicationInfo info = applist.get(dialogId);
        broadcastColorChange(info, color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}

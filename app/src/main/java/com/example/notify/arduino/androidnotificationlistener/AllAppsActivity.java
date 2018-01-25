package com.example.notify.arduino.androidnotificationlistener;

import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class AllAppsActivity extends ListActivity implements ColorPickerDialogListener {

    private String TAG = this.getClass().getSimpleName();

    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listAdapter = null;
    protected static final String APP_SELECTED_ACTION = "com.example.notify.arduino" +
            ".androidnotificationlistener.APP_SELECTED_ACTION";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_apps);

        packageManager = getPackageManager();

        new LoadApplications().execute();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = applist.get(position);

        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);
            intent.putExtra("app", app);

            ColorPickerDialog.newBuilder().setColor(Color.BLUE).setDialogId(position).show(this);

//            if (intent != null) {
//                startActivity(intent);
//            }
            //Intent intent = new Intent(this, )
        } catch (Exception e) {
            Toast.makeText(AllAppsActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<>();
        for (ApplicationInfo info : list) {
            try {
                if (packageManager.getLaunchIntentForPackage(info.packageName) != null) {
                    applist.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return applist;
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        Log.i(TAG,"**********onColorSelected**********");
        ApplicationInfo applicationInfo = applist.get(dialogId);
        Log.i(TAG, "Selected App: " + applicationInfo.packageName);

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        Log.i(TAG, "Color(Hex): " + Integer.toHexString(color));
        Log.i(TAG, "R: " + red + "\tG: " + green + "\tB: " + blue);

        Intent intent = new Intent(APP_SELECTED_ACTION);
        intent.putExtra("package_name", applicationInfo.packageName);
        intent.putExtra("color", color);
        sendBroadcast(intent);

        Intent resultIntent = new Intent(APP_SELECTED_ACTION);
        resultIntent.putExtra("app_info", applicationInfo);
        resultIntent.putExtra("color", color);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        return;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listAdapter = new ApplicationAdapter(AllAppsActivity.this,
                    R.layout.app_select_list_row, applist);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listAdapter);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(AllAppsActivity.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
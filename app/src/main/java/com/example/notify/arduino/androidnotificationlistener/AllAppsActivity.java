package com.example.notify.arduino.androidnotificationlistener;

import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;

public class AllAppsActivity extends ListActivity {
    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listAdapter = null;

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

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;

//        switch (item.getItemId()) {
//            case R.id.menu_about: {
//                displayAboutDialog();
//
//                break;
//            }
//            default: {
//                result = super.onOptionsItemSelected(item);
//
//                break;
//            }
//        }

        return result;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = applist.get(position);

        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);
            intent.putExtra("app", app);

            ColorPickerDialog.newBuilder().setColor(Color.GREEN).show(this);

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
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
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

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listAdapter = new ApplicationAdapter(AllAppsActivity.this,
                    R.layout.app_list_row, applist);

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
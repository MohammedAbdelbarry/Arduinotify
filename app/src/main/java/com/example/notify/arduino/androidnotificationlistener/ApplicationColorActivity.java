package com.example.notify.arduino.androidnotificationlistener;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;


public class ApplicationColorActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();
    private static final int COLOR_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_color);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void fabClicked(View view) {
        Log.i(TAG, "**********fabClicked**********");
        Intent intent = new Intent(this, AllAppsActivity.class);
        startActivityForResult(intent, COLOR_REQUEST_CODE);
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
                
            }
        }
    }
}

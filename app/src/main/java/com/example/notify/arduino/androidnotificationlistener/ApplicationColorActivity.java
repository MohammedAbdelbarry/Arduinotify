package com.example.notify.arduino.androidnotificationlistener;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ApplicationColorActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_color);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void fabClicked(View view) {
        Log.i(TAG, "**********fabClicked**********");
        Intent intent = new Intent(this, AllAppsActivity.class);
        startActivity(intent);
    }

//    public static class ApplicationListDialog extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//            List<ApplicationInfo> pkgAppsList = getContext().getPackageManager().queryIntentActivities(mainIntent, 0);
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
////            builder.setTitle(R.string.pick_color)
////                    .setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
////                        public void onClick(DialogInterface dialog, int which) {
////                            // The 'which' argument contains the index position
////                            // of the selected item
////                        }
////                    });
//            return builder.create();
//        }
//    }
}

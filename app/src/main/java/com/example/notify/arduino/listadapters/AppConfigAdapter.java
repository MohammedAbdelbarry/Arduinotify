package com.example.notify.arduino.listadapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.notify.arduino.androidnotificationlistener.AppConfig;
import com.example.notify.arduino.androidnotificationlistener.R;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by mohammed on 1/26/18.
 */

public class AppConfigAdapter extends ArrayAdapter<AppConfig> {
    private List<AppConfig> appsList = null;
    private Context context;
    private PackageManager packageManager;

    public AppConfigAdapter(Context context, int textViewResourceId,
                              List<AppConfig> appsList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return ((appsList != null) ? appsList.size() : 0);
    }

    @Override
    public AppConfig getItem(int position) {
        return ((appsList != null) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.app_select_list_row, null);
        }

        ApplicationInfo applicationInfo = appsList.get(position).applicationInfo;
        int color = appsList.get(position).color;
        if (null != applicationInfo) {
            TextView appName = (TextView) view.findViewById(R.id.app_name);
            ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);
            TextView packageName = (TextView) view.findViewById(R.id.package_name);
            appName.setText(applicationInfo.loadLabel(packageManager));
            iconview.setImageDrawable(applicationInfo.loadIcon(packageManager));
            packageName.setText(applicationInfo.packageName);
            int len = 100;
            int[] grad = new int [len];
            grad[len - 2] = grad[len - 1] = color;
            view.setBackground(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, grad));
        }
        return view;
    }
}

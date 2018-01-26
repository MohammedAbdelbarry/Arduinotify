package com.example.notify.arduino.listadapters;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notify.arduino.androidnotificationlistener.R;

import org.w3c.dom.Text;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
    private List<ApplicationInfo> appsList = null;
    private Context context;
    private PackageManager packageManager;

    public ApplicationAdapter(Context context, int textViewResourceId,
                              List<ApplicationInfo> appsList) {
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
    public ApplicationInfo getItem(int position) {
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

        ApplicationInfo applicationInfo = appsList.get(position);
        if (null != applicationInfo) {
            TextView appName = (TextView) view.findViewById(R.id.app_name);
            ImageView iconview = (ImageView) view.findViewById(R.id.app_icon);
            TextView packageName = (TextView) view.findViewById(R.id.package_name);
            appName.setText(applicationInfo.loadLabel(packageManager));
            iconview.setImageDrawable(applicationInfo.loadIcon(packageManager));
            packageName.setText(applicationInfo.packageName);
        }
        return view;
    }
};

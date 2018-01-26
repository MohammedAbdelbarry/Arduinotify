package com.example.notify.arduino.androidnotificationlistener;

import android.content.pm.ApplicationInfo;
import android.support.annotation.ColorInt;

/**
 * Created by mohammed on 1/26/18.
 */

public class AppConfig {

    public ApplicationInfo applicationInfo;
    public @ColorInt int color;

    public AppConfig() {

    }

    public AppConfig(ApplicationInfo info, int color) {
        this.applicationInfo = info;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppConfig appConfig = (AppConfig) o;

        if (color != appConfig.color) return false;
        return applicationInfo != null ? applicationInfo.equals(appConfig.applicationInfo) : appConfig.applicationInfo == null;

    }

    @Override
    public int hashCode() {
        int result = applicationInfo != null ? applicationInfo.hashCode() : 0;
        result = 31 * result + color;
        return result;
    }
}

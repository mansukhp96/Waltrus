package com.izmansuk.securepasswordmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class UtilsHelper {

    public static void saveStringSharedPrefs(Context context, String key, String val) {
        SharedPreferences.Editor editor = context.getSharedPreferences("shared", Activity.MODE_PRIVATE).edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static String getStringSharedPrefs(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared", Activity.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
}

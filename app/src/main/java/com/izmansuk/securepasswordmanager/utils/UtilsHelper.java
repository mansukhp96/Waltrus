package com.izmansuk.securepasswordmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class UtilsHelper {

    public static void saveStringSharedPrefs(Context context, String key, String val) throws GeneralSecurityException, IOException {
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                "masterKey",
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        SharedPreferences.Editor editor = context.getSharedPreferences("shared", Activity.MODE_PRIVATE).edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static String getStringSharedPrefs(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared", Activity.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
}

package com.izmansuk.securepasswordmanager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
<<<<<<< HEAD
import androidx.security.crypto.MasterKeys;
=======
>>>>>>> 2d8d8b87b89d8cd5dee12763c0e35848fc24ef37

import java.io.IOException;
import java.security.GeneralSecurityException;


public class UtilsHelper {

<<<<<<< HEAD
    public static SharedPreferences getEncryptedSharedPreferences(Context context) throws GeneralSecurityException, IOException {

        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
=======
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
>>>>>>> 2d8d8b87b89d8cd5dee12763c0e35848fc24ef37

        return EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}

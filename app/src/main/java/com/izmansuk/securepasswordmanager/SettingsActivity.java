package com.izmansuk.securepasswordmanager;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.izmansuk.securepasswordmanager.ui.main.SettingsFragment;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        //loadSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static int getPasswordLen(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("pwd_length_key", "10"));
    }

    public static boolean isSpecialChar(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pwd_special_key", false);
    }

    public static boolean isUpperChar(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pwd_upper_key", true);
    }

    public static boolean isLowerChar(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pwd_lower_key", true);
    }

    public static boolean isNumberChar(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pwd_number_key", true);
    }

    private void loadSettings() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String pwdLen = prefs.getString("pwd_length_key", "8");
        boolean specialChar = prefs.getBoolean("pwd_special_key", true);
        boolean upperChar = prefs.getBoolean("pwd_upper_key", true);
        boolean lowerChar = prefs.getBoolean("pwd_lower_key", true);
        boolean numberChar = prefs.getBoolean("pwd_number_key", true);

        Log.e("SETT_VALS", "pwd_length: " + String.valueOf(pwdLen));
        //Log.e("SETT_VALS", "special: " + String.valueOf(specialChar));
        //Log.e("SETT_VALS", "upper: " + String.valueOf(upperChar));
        //Log.e("SETT_VALS", "lower: " + String.valueOf(lowerChar));
        //Log.e("SETT_VALS", "numbers: " + String.valueOf(numberChar));
    }
}
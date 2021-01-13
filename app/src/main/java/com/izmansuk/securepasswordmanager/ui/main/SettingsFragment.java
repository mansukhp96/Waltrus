package com.izmansuk.securepasswordmanager.ui.main;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.izmansuk.securepasswordmanager.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_prefs, rootKey);
    }
}
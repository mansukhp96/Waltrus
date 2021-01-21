package com.izmansuk.securepasswordmanager.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.izmansuk.securepasswordmanager.R;
import com.izmansuk.securepasswordmanager.SetMasterPasswordActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_prefs, rootKey);
    }
}
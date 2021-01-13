package com.izmansuk.securepasswordmanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.izmansuk.securepasswordmanager.ui.main.SettingsFragment;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
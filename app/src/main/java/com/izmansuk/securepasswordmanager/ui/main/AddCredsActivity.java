package com.izmansuk.securepasswordmanager.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import net.sqlcipher.database.SQLiteDatabase;

import com.google.android.material.snackbar.Snackbar;
import com.izmansuk.securepasswordmanager.R;

public class AddCredsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        SQLiteDatabase.loadLibs(AddCredsActivity.this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("New Credentials");

        }
        setContentView(R.layout.activity_add_creds);

        EditText label = findViewById(R.id.edTxtLabel);
        EditText domain = findViewById(R.id.edTxtDomain);
        EditText username = findViewById(R.id.edTxtUsername);
        EditText password = findViewById(R.id.edTxtPassword);

        Button addToVaultBtn = findViewById(R.id.BtnAddtoVault);
        Button cancelBtn = findViewById(R.id.BtnCancel);

        ListView vaultList = (ListView)findViewById(R.id.credsList);

        addToVaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmptyField(label)
                        && !isEmptyField(domain)
                        && !isEmptyField(username)
                        && !isEmptyField(password)) {
                    Log.e("INPUTS","LABEL: " + label.getText());
                    Log.e("INPUTS","DOMAIN: " + domain.getText());
                    Log.e("INPUTS","USERNAME: " + username.getText());
                    Log.e("INPUTS","PASSWORD: " + password.getText());

                    //Insert into db
                    DBHelper.getInstance(AddCredsActivity.this).insertCredentials(
                            label.getText().toString(),
                            domain.getText().toString(),
                            username.getText().toString(),
                            password.getText().toString());
                    finish();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //other elements/items
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isEmptyField(EditText field) {
        if (field.getText().toString().isEmpty()
                || field.getText().toString().equals("")
                || field.getText().toString().equals(" ")
                || field.getText().toString() == null) {
            field.requestFocus();
            field.setError(field.getContentDescription() + " can't be empty!");
            return true;
        }
        else {
            return false;
        }
    }

}
package com.izmansuk.securepasswordmanager.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.izmansuk.securepasswordmanager.R;

import net.sqlcipher.database.SQLiteDatabase;

public class EditCredsActivity extends AppCompatActivity {

    public String labelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        SQLiteDatabase.loadLibs(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Credentials");
        }
        setContentView(R.layout.activity_edit_creds);

        EditText label = findViewById(R.id.edTxtLabel);
        labelId = getIntent().getStringExtra("label");
        label.setText(labelId);

        EditText domain = findViewById(R.id.edTxtDomain);
        EditText username = findViewById(R.id.edTxtUsername);
        EditText password = findViewById(R.id.edTxtPassword);

        Button saveChangesBtn = findViewById(R.id.BtnSaveToVault);
        Button deleteFromVault = findViewById(R.id.BtnDeleteCreds);
        Button cancelBtn = findViewById(R.id.BtnCancel);

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Changes saved!" + labelId, Toast.LENGTH_SHORT).show();
            }
        });

        deleteFromVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean res = DBHelper.getInstance(EditCredsActivity.this).deleteCredentials(labelId);

                if(res) {
                    Intent retIntnt = new Intent();
                    retIntnt.putExtra("result", labelId);
                    setResult(Activity.RESULT_OK, retIntnt);

                    finish();
                }
                else
                    Toast.makeText(EditCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
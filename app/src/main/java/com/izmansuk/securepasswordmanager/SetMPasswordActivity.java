package com.izmansuk.securepasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toolbar;

public class SetMPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Master Password Settings");
        }
        setContentView(R.layout.activity_set_master_password);

        EditText email = findViewById(R.id.edTxtEmail);
        EditText mPassword = findViewById(R.id.edTxtMpassword);
        EditText reMPassword = findViewById(R.id.edTxtReMpassword);

        Button saveBtn = findViewById(R.id.btnSave);
        Button cancelBtn = findViewById(R.id.btnSettCancel);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEmptyField(email) //check empty email
                        && !isEmptyField(mPassword) //check empty Mpass
                        && !isEmptyField(reMPassword) //check empty ReMpass
                        && !isMinPwd(mPassword) //check Mpass > 8 char //Check if Mpass-ReMpass match
                        && !isPwdMatch(mPassword, reMPassword)) {
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

    private boolean isPwdMatch(EditText field1, EditText field2) {
        if (!field1.getText().toString().equals(field2.getText().toString())) {
            field2.requestFocus();
            field2.setError("Passwords don't match!");
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isMinPwd(EditText field) {
        if (field.getText().toString().length() < 8) {
            field.requestFocus();
            field.setError("Master Password too short!");
            return true;
        }
        else {
            return false;
        }
    }
}
package com.izmansuk.securepasswordmanager.activityscreens;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.izmansuk.securepasswordmanager.utils.AESHelper;
import com.izmansuk.securepasswordmanager.utils.DBHelper;
import com.izmansuk.securepasswordmanager.R;

import net.sqlcipher.database.SQLiteDatabase;

public class EditCredsActivity extends AppCompatActivity {

    public String labelId;

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        TextView label = findViewById(R.id.edTxtLabel);
        labelId = getIntent().getStringExtra("label");
        label.setText(labelId);

        EditText domain = findViewById(R.id.edTxtDomain);
        domain.setText(DBHelper.getInstance(this).getDomain(labelId));

        EditText username = findViewById(R.id.edTxtUsername);
        username.setText(DBHelper.getInstance(this).getUsername(labelId));

        EditText password = findViewById(R.id.edTxtPassword);
        //
        Log.e("E-PASSWORD", password.getText().toString());
        String temp = AESHelper.decrypt(DBHelper.getInstance(this).getPassword(labelId), EditCredsActivity.this);
        password.setText(temp);

        Button saveChangesBtn = findViewById(R.id.BtnSaveToVault);
        Button deleteFromVault = findViewById(R.id.BtnDeleteCreds);

        //
        Log.e("E-USERNAME", username.getText().toString());
        Log.e("E-WEBSITE", domain.getText().toString());

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmptyField(domain)
                        && !isEmptyField(username)
                        && !isEmptyField(password)) {

                    //Insert into db
                    Boolean res = DBHelper.getInstance(EditCredsActivity.this).updateCredentials(
                            label.getText().toString(),
                            domain.getText().toString(),
                            username.getText().toString(),
                            AESHelper.encrypt(password.getText().toString(), EditCredsActivity.this));

                    if(res) {
                        Intent retIntnt = new Intent();
                        retIntnt.putExtra("result", label.getText().toString());
                        setResult(Activity.RESULT_OK, retIntnt);

                        finish();
                    }
                    else
                        Toast.makeText(EditCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteFromVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean res = DBHelper.getInstance(EditCredsActivity.this).deleteCredentials(labelId);

                if(res) {
                    Intent retIntnt = new Intent();
                    retIntnt.putExtra("result", labelId);
                    setResult(Activity.RESULT_FIRST_USER, retIntnt);

                    finish();
                }
                else
                    Toast.makeText(EditCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();
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
}
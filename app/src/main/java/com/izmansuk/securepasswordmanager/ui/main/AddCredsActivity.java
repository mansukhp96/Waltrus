package com.izmansuk.securepasswordmanager.ui.main;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.izmansuk.securepasswordmanager.R;

public class AddCredsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        SQLiteDatabase.loadLibs(this);

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

        addToVaultBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(!isEmptyField(label)
                        && !isEmptyField(domain)
                        && !isEmptyField(username)
                        && !isEmptyField(password)) {

                    //Insert into db
                    Boolean res = DBHelper.getInstance(AddCredsActivity.this).insertCredentials(
                            label.getText().toString(),
                            domain.getText().toString(),
                            username.getText().toString(),
                            AESHelper.encrypt(password.getText().toString()));

                    if(res) {
                        Intent retIntnt = new Intent();
                        retIntnt.putExtra("result", label.getText().toString());
                        setResult(Activity.RESULT_OK, retIntnt);
                        
                        finish();
                    }
                    else
                        Toast.makeText(AddCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();
                }
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
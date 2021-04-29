package com.izmansuk.securepasswordmanager.activityscreens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import net.sqlcipher.database.SQLiteDatabase;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.izmansuk.securepasswordmanager.utils.AESHelper;
import com.izmansuk.securepasswordmanager.utils.DBHelper;
import com.izmansuk.securepasswordmanager.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.SecretKey;

public class AddCredsActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(AddCredsActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                try {
                    KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);
                    SecretKey secretKey = (SecretKey) keyStore.getKey("Key", null);

                    //Insert into db
                    Boolean res = DBHelper.getInstance(AddCredsActivity.this).insertCredentials(
                            AddCredsActivity.this,
                            label.getText().toString(),
                            domain.getText().toString(),
                            username.getText().toString(),
                            AESHelper.encrypt(password.getText().toString(), AddCredsActivity.this, secretKey));

                    if(res) {
                        Intent retIntnt = new Intent();
                        retIntnt.putExtra("result", label.getText().toString());
                        setResult(Activity.RESULT_OK, retIntnt);

                        finish();
                    }
                    else
                        Toast.makeText(AddCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(),
                            "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                    finish();

                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Confirm adding new credential using biometric authentication")
                .setNegativeButtonText("Cancel")
                .build();

        addToVaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmptyField(label)
                        && !isEmptyField(domain)
                        && !isEmptyField(username)
                        && !isEmptyField(password)) {

                    Log.e("A-USERNAME", username.getText().toString());
                    Log.e("A-PASSWORD", password.getText().toString());
                    Log.e("A-WEBSITE", domain.getText().toString());

                    biometricPrompt.authenticate(promptInfo);
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
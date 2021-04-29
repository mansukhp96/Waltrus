package com.izmansuk.securepasswordmanager.activityscreens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import androidx.lifecycle.LifecycleObserver;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.izmansuk.securepasswordmanager.utils.AESHelper;
import com.izmansuk.securepasswordmanager.utils.DBHelper;
import com.izmansuk.securepasswordmanager.R;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.Executor;

import javax.crypto.SecretKey;

public class EditCredsActivity extends AppCompatActivity {

    public String labelId;

    private Executor executorEdit;
    private Executor executorDecrypt;
    private BiometricPrompt biometricDecryptPrompt;
    private BiometricPrompt biometricEditPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private LifecycleObserver lastLifecycleObserver;

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

        try {
            EditText domain = findViewById(R.id.edTxtDomain);
            domain.setText(DBHelper.getInstance(this).getDomain(this, labelId));

            EditText username = findViewById(R.id.edTxtUsername);
            username.setText(DBHelper.getInstance(this).getUsername(this, labelId));

            EditText password = findViewById(R.id.edTxtPassword);
            ImageButton copyButton = findViewById(R.id.copyButton2);

            String temp = DBHelper.getInstance(this).getPassword(this, labelId);
            password.setText(temp);

            executorDecrypt = ContextCompat.getMainExecutor(this);
            biometricDecryptPrompt = new BiometricPrompt(EditCredsActivity.this,
                    executorDecrypt, new BiometricPrompt.AuthenticationCallback() {
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

                        String decrypPassword = AESHelper.decrypt(password.getText().toString(), EditCredsActivity.this, secretKey);
                        password.setText(decrypPassword);

                    } catch (IOException
                            | GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(getApplicationContext(), "Failed to authenticate user!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });

            executorEdit = ContextCompat.getMainExecutor(this);
            biometricEditPrompt = new BiometricPrompt(EditCredsActivity.this,
                    executorEdit, new BiometricPrompt.AuthenticationCallback() {
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

                        //Update into db
                    Boolean res = DBHelper.getInstance(EditCredsActivity.this).updateCredentials(
                            EditCredsActivity.this,
                            label.getText().toString(),
                            domain.getText().toString(),
                            username.getText().toString(),
                            AESHelper.encrypt(password.getText().toString(), EditCredsActivity.this, secretKey));

                    if(res) {
                        Intent retIntnt = new Intent();
                        retIntnt.putExtra("result", label.getText().toString());
                        setResult(Activity.RESULT_OK, retIntnt);

                        finish();
                    }
                    else
                        Toast.makeText(EditCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();


                    } catch (IOException
                            | GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(getApplicationContext(), "Failed to authenticate user!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });

        Button saveChangesBtn = findViewById(R.id.BtnSaveToVault);
        Button deleteFromVault = findViewById(R.id.BtnDeleteCreds);


        Log.e("E-USERNAME", username.getText().toString());
        Log.e("E-WEBSITE", domain.getText().toString());

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Confirm changes to credential using biometric authentication")
                .setNegativeButtonText("Cancel")
                .build();


        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout2);
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricDecryptPrompt.authenticate(promptInfo);
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!password.getText().toString().equals("") || password.getText() == null) {
                    ClipData clipData = ClipData.newPlainText("credPass", password.getText().toString());
                    ClipboardManager clipBrd = (ClipboardManager) EditCredsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipBrd.setPrimaryClip(clipData);
                    Snackbar.make(v, "Copied!", Snackbar.LENGTH_SHORT).setAction("Copy action", null).show();
                }
            }
        });

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmptyField(domain)
                        && !isEmptyField(username)
                        && !isEmptyField(password)) {

                    biometricEditPrompt.authenticate(promptInfo);
                }
            }
        });

        deleteFromVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Boolean res = DBHelper.getInstance(EditCredsActivity.this).deleteCredentials(EditCredsActivity.this, labelId);

                    if(res) {
                        Intent retIntnt = new Intent();
                        retIntnt.putExtra("result", labelId);
                        setResult(Activity.RESULT_FIRST_USER, retIntnt);

                        finish();
                    }
                    else
                        Toast.makeText(EditCredsActivity.this, "Failed, Try again!", Toast.LENGTH_SHORT).show();
                }
                catch (GeneralSecurityException
                        | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        }
        catch (GeneralSecurityException
                | IOException e) {
            e.printStackTrace();
        }
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
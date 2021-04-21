package com.izmansuk.securepasswordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SetMPasswordActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(SetMPasswordActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                //encrypt and store in android keystore
                try {
                    String passwordString = mPassword.getText().toString();
                    SecretKey secretKey = makeKey();
                    Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                            + "/"
                            + KeyProperties.BLOCK_MODE_CBC
                            + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                    byte[] encryptionIv = cipher.getIV();
                    byte[] passwordBytes = passwordString.getBytes("UTF-8");
                    byte[] encryptedPasswordBytes = cipher.doFinal(passwordBytes);
                    String encryptedPassword = Base64.encodeToString(encryptedPasswordBytes, Base64.DEFAULT);

                    //Store encrypted password and IV in shared prefs
                    UtilsHelper.saveStringSharedPrefs(SetMPasswordActivity.this, "encMasterPasswd", encryptedPassword);
                    UtilsHelper.saveStringSharedPrefs(SetMPasswordActivity.this, "encryptionIV", Base64.encodeToString(encryptionIv, Base64.DEFAULT));

                    Log.e("XXXQ", encryptedPassword);
                    Log.e("XXXQ", Base64.encodeToString(encryptionIv, Base64.DEFAULT));

                } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                finish();
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
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEmptyField(email) //check empty email
                        && !isEmptyField(mPassword) //check empty Mpass
                        && !isEmptyField(reMPassword) //check empty ReMpass
                        && !isMinPwd(mPassword) //check Mpass > 8 char //Check if Mpass-ReMpass match
                        && !isPwdMatch(mPassword, reMPassword)) {

                    biometricPrompt.authenticate(promptInfo);
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

    private SecretKey makeKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keygen.init(new KeyGenParameterSpec.Builder("Key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(20)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            return keygen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
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
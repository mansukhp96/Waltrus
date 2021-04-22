package com.izmansuk.securepasswordmanager.activityscreens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.izmansuk.securepasswordmanager.utils.AESHelper;
import com.izmansuk.securepasswordmanager.R;
import com.izmansuk.securepasswordmanager.utils.UtilsHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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

                try {
                    String passwordString = mPassword.getText().toString();
                    SecretKey secretKey = AESHelper.generateSecretKey();
                    Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                            + "/"
                            + KeyProperties.BLOCK_MODE_CBC
                            + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                    byte[] encryptionIv = cipher.getIV();
                    byte[] passwordBytes = passwordString.getBytes(StandardCharsets.UTF_8);
                    byte[] encryptedPasswordBytes = cipher.doFinal(passwordBytes);
                    String encryptedPassword = Base64.encodeToString(encryptedPasswordBytes, Base64.DEFAULT);

                    //Store encrypted password and IV in encrypted shared prefs
                    UtilsHelper.getEncryptedSharedPreferences(SetMPasswordActivity.this)
                            .edit()
                            .putString("encMasterPasswd", encryptedPassword)
                            .apply();

                    UtilsHelper.getEncryptedSharedPreferences(SetMPasswordActivity.this)
                            .edit()
                            .putString("Mansukh", "MansukhLoves")
                            .apply();

                    UtilsHelper.getEncryptedSharedPreferences(SetMPasswordActivity.this)
                            .edit()
                            .putString("encryptionIV", Base64.encodeToString(encryptionIv, Base64.DEFAULT))
                            .apply();

                    Log.e("XXXQ", encryptedPassword);
                    Log.e("XXXQ", secretKey.toString());
                    Log.e("XXXQ", Base64.encodeToString(encryptionIv, Base64.DEFAULT));

                } catch (GeneralSecurityException | IOException e) {
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
                .setTitle("Biometric Authentication")
                .setSubtitle("Confirm this change using biometric authentication")
                .setNegativeButtonText("Cancel")
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
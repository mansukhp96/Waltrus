package com.izmansuk.securepasswordmanager.ui.mainscreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.izmansuk.securepasswordmanager.activityscreens.AddCredsActivity;
import com.izmansuk.securepasswordmanager.utils.DBHelper;
import com.izmansuk.securepasswordmanager.activityscreens.EditCredsActivity;
import com.izmansuk.securepasswordmanager.R;
import com.izmansuk.securepasswordmanager.activityscreens.SettingsActivity;
import com.izmansuk.securepasswordmanager.utils.UtilsHelper;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Vault tab section fragment
 */
public class VaultFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public ArrayList<String> vltListItems = new ArrayList<String>();
    public ArrayAdapter vltLstAdp;
    ListView vltData;
    SwitchCompat passSwch;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public static VaultFragment newInstance(int index) {
        VaultFragment vaultFrag = new VaultFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        //vaultFrag.setArguments(bundle);
        return vaultFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SQLiteDatabase.loadLibs(getContext());
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);

        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Vault Authentication")
                .setSubtitle("Unlock vault using biometric authentication")
                .setNegativeButtonText("Cancel")
                .build();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        try {
            View root = inflater.inflate(R.layout.fragment_vault, container, false);
            vltData = root.findViewById(R.id.credsList);

            vltLstAdp = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, vltListItems);
            vltData.setAdapter(vltLstAdp);
            vltListItems.addAll(DBHelper.getInstance(getContext()).getAllData(root.getContext()));
            passSwch = root.findViewById(R.id.passwordSwitch);

            final TextView textView = root.findViewById(R.id.section_label);
            textView.setText(R.string.vault_header);

            //Floating actionbar - new credentials activity
            fabOnClickAddCredsAct(root);

            //Prompt Master Password dialog on toggle of Vault show switch
            pwdSwitchToggleMPwdPrompt(root);

        vltData.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //New Activity Intent To edit or delete creds item
                if(passSwch.isChecked()){
                    Intent editCredsIntent = new Intent(getContext(), EditCredsActivity.class);
                    editCredsIntent.putExtra("label", vltListItems.get(position));

                    startActivityForResult(editCredsIntent, 20);
                }
            }

        });

        vltData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Browser link to domain
                if (passSwch.isChecked()){
                    try {
                        String link = DBHelper.getInstance(getContext()).getDomain(getContext(), vltListItems.get(position));
                        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(linkIntent);
                    } catch (GeneralSecurityException
                            | IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                else
                    return false;
            }
        });

        executor = ContextCompat.getMainExecutor(getContext());
        biometricPrompt = new BiometricPrompt((FragmentActivity) getContext(),
                executor, new BiometricPrompt.AuthenticationCallback() {

            int lockDur = SettingsActivity.getAutoLockDuration(getContext());
            ImageView vis = root.findViewById(R.id.visImg);

            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                passSwch.setChecked(false);
                vis.setVisibility(View.INVISIBLE);

                Toast.makeText(getContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                //Master Password Prompt
                LayoutInflater pwdPrompt = LayoutInflater.from(getActivity());
                View pwdPromptView = pwdPrompt.inflate(R.layout.mpassword_prompt, null);

                AlertDialog.Builder pwdPromptBldr = new AlertDialog.Builder(getActivity());
                pwdPromptBldr.setView(pwdPromptView);

                //Password is here from inp
                final EditText pwdInp = (EditText) pwdPromptView.findViewById(R.id.edTxtPromptMpassword);

                //logic
                try {
                    String base64EncMPasswd = UtilsHelper.getEncryptedSharedPreferences(getContext())
                            .getString("encMasterPasswd", null);

                    String base64EncIv = UtilsHelper.getEncryptedSharedPreferences(getContext())
                            .getString("encryptionIV", null);

                    Log.e("TEST", base64EncIv + "!");
                    Log.e("TEST", base64EncMPasswd + "!");

                    byte[] encryptionIv = Base64.decode(base64EncIv, Base64.DEFAULT);
                    byte[] encryptedMPasswd = Base64.decode(base64EncMPasswd, Base64.DEFAULT);

                    KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);
                    SecretKey secretKey = (SecretKey) keyStore.getKey("Key", null);
                    Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                            + "/"
                            + KeyProperties.BLOCK_MODE_CBC
                            + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(encryptionIv));

                    byte[] mPasswordBytes = cipher.doFinal(encryptedMPasswd);
                    String mPassword = new String(mPasswordBytes, StandardCharsets.UTF_8);

                    pwdPromptBldr.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (pwdInp.getText().toString().equals(mPassword)) {

                                passSwch.setChecked(true);
                                vis.setVisibility(View.VISIBLE);
                                vltData.setLongClickable(true);

                                //60 seconds to lock the vault
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        passSwch.setChecked(false);
                                        vis.setVisibility(View.INVISIBLE);
                                    }
                                }, lockDur * 60000);
                            }
                            else {
                                passSwch.setChecked(false);
                                dialog.cancel();
                                Toast.makeText(getContext(), "Incorrect Master Password/OTP", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            passSwch.setChecked(false);
                            vis.setVisibility(View.INVISIBLE);
                            dialog.cancel();
                        }
                    });

                    AlertDialog pwdAlert = pwdPromptBldr.create();
                    pwdAlert.show();


                    Log.e("THISMPASS", mPassword);
                    Log.e("THISIV", encryptionIv.toString());
                    Log.e("THISKEY", secretKey.toString());

                } catch (IOException
                        | GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getContext(), "2F Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        return root;

        } catch (GeneralSecurityException
                | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10) {
            if(resultCode == Activity.RESULT_OK) {
                String res = data.getStringExtra("result");
                vltListItems.add(res);
                vltLstAdp.notifyDataSetChanged();
                Toast.makeText(getContext(), "Credentials added to vault", Toast.LENGTH_SHORT).show();
            }
            //else?
        }
        if(requestCode == 20) {
            if(resultCode == Activity.RESULT_FIRST_USER) {
                String res = data.getStringExtra("result");
                vltListItems.remove(res);
                vltLstAdp.notifyDataSetChanged();
                Toast.makeText(getContext(), "Credentials deleted from vault", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getContext(), "Credentials updated in vault", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fabOnClickAddCredsAct(View root) {
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Intent AddCredsIntent = new Intent(getContext(), AddCredsActivity.class);
                startActivityForResult(AddCredsIntent, 10);
            }
        });
    }

    private void getOTPOnClickVerifyMPwd(Button getOTP, EditText pwdInp, CountDownTimer counter) {
        //GetOTP button click listener - Send OTP
        getOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pwdInp.getText().toString().equals("ABC")) {
                    pwdInp.requestFocus();
                    pwdInp.setError("Incorrect Master Password!");
                }
                else {
                    counter.start();
                    getOTP.setEnabled(false);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getOTP.setEnabled(true);
                        }
                    }, 15000);
                }
            }
        });
    }

    private CountDownTimer getOTPOnClickCountDown(Button getOTP) {
        return new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getOTP.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                getOTP.setText("GET OTP");
            }
        };
    }

    //On Switch toggle
    private void pwdSwitchToggleMPwdPrompt(View root) {

        ImageView vis = root.findViewById(R.id.visImg);
        vis.setVisibility(View.INVISIBLE);

        passSwch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    //Prompt biometric and decrypt master password
                    biometricPrompt.authenticate(promptInfo);
                }
                else {
                    vis.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
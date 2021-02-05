package com.izmansuk.securepasswordmanager.ui.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.izmansuk.securepasswordmanager.R;

/**
 * Vault tab section fragment
 */
public class VaultFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    private LayoutInflater inflater;
    private ViewGroup container;

    public static VaultFragment newInstance(int index) {
        VaultFragment vaultFrag = new VaultFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        //vaultFrag.setArguments(bundle);
        return vaultFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_vault, container, false);

        final TextView textView = root.findViewById(R.id.section_label);
        textView.setText(R.string.vault_header);

        //Floating actionbar - new credentials activity
        fabOnClickAddCredsAct(root);

        //Prompt Master Password dialog on toggle of Vault show switch
        pwdSwitchToggleMPwdPrompt(root);

        return root;
    }

    //Floating Action Bar
    private void fabOnClickAddCredsAct(View root) {
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddCredsActivity.class));
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
        CountDownTimer counter = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getOTP.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                getOTP.setText("GET OTP");
            }
        };
        return counter;
    }

    //On Switch toggle
    private void pwdSwitchToggleMPwdPrompt(View root) {

        ImageView vis = root.findViewById(R.id.visImg);
        vis.setVisibility(View.INVISIBLE);
        Switch passSwch = root.findViewById(R.id.passwordSwitch);

        passSwch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    LayoutInflater pwdPrompt = LayoutInflater.from(getActivity());
                    View pwdPromptView = pwdPrompt.inflate(R.layout.mpassword_prompt, null);

                    AlertDialog.Builder pwdPromptBldr = new AlertDialog.Builder(getActivity());
                    pwdPromptBldr.setView(pwdPromptView);

                    //Password is here from inp
                    final EditText pwdInp = (EditText) pwdPromptView.findViewById(R.id.edTxtPromptMpassword);
                    EditText otpInp = (EditText) pwdPromptView.findViewById(R.id.edTxtPromptOTP);
                    Button getOTP = pwdPromptView.findViewById(R.id.BtnOTP);

                    //Count down timer for getOTP button on click
                    CountDownTimer counter = getOTPOnClickCountDown(getOTP);

                    //Verify Master Password on click of getOTP
                    getOTPOnClickVerifyMPwd(getOTP, pwdInp, counter);

                    pwdPromptBldr.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Check if the password salt hash is the same. Right -> continue; Else -> return
                            //Check OTP
                            if (pwdInp.getText().toString().equals("ABC")
                                    && otpInp.getText().toString().equals("123")) {
                                passSwch.setChecked(true);
                                vis.setVisibility(View.VISIBLE);

                                //60 seconds to lock the vault
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        passSwch.setChecked(false);
                                        vis.setVisibility(View.INVISIBLE);
                                    }
                                }, 60000);

                            }
                            else {
                                passSwch.setChecked(false);
                                dialog.cancel();
                                Snackbar.make(root, "Incorrect Master Password or OTP! Try again", Snackbar.LENGTH_LONG).setAction("try again", null).show();
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
                }
                else {
                    vis.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
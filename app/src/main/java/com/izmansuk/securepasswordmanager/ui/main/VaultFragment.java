package com.izmansuk.securepasswordmanager.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.izmansuk.securepasswordmanager.R;
import com.izmansuk.securepasswordmanager.SettingsActivity;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Vault tab section fragment
 */
public class VaultFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public ArrayList<String> vltListItems = new ArrayList<String>();
    public ArrayAdapter vltLstAdp;
    ListView vltData;

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
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_vault, container, false);
        vltData = root.findViewById(R.id.credsList);

        vltLstAdp = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, vltListItems);
        vltData.setAdapter(vltLstAdp);
        vltListItems.addAll(DBHelper.getInstance(getContext()).getAllData());

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
                Intent editCredsIntent = new Intent(getContext(), EditCredsActivity.class);
                editCredsIntent.putExtra("label", vltListItems.get(position));

                startActivityForResult(editCredsIntent, 20);
            }
        });

        return root;
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
            if(resultCode == Activity.RESULT_OK) {
                String res = data.getStringExtra("result");
                vltListItems.remove(res);
                vltLstAdp.notifyDataSetChanged();
                Toast.makeText(getContext(), "Credentials deleted from vault", Toast.LENGTH_SHORT).show();
            }
            //else?
        }
    }

    private void fabOnClickAddCredsAct(View root) {
        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), AddCredsActivity.class), 10);
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
        SwitchCompat passSwch = root.findViewById(R.id.passwordSwitch);

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
                    int lockDur = SettingsActivity.getAutoLockDuration(getContext());

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
                }
                else {
                    vis.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
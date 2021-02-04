package com.izmansuk.securepasswordmanager.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.google.android.material.tabs.TabLayout;
import com.izmansuk.securepasswordmanager.MainActivity;
import com.izmansuk.securepasswordmanager.R;
import com.izmansuk.securepasswordmanager.SetMPasswordActivity;

/**
 * Vault tab section fragment
 */
public class VaultFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

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

        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);
        ImageView vis = root.findViewById(R.id.visImg);
        vis.setVisibility(View.INVISIBLE);
        Switch passSwch = root.findViewById(R.id.passwordSwitch);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "Your new password is safe!", Snackbar.LENGTH_LONG).setAction("Add action", null).show();
                startActivity(new Intent(getActivity(), AddCredsActivity.class));
            }
        });

        passSwch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LayoutInflater pwdPrompt = LayoutInflater.from(getActivity());
                    View pwdPromptView = pwdPrompt.inflate(R.layout.mpassword_prompt, null);

                    AlertDialog.Builder pwdPromptBldr = new AlertDialog.Builder(getActivity());
                    pwdPromptBldr.setView(pwdPromptView);

                    //Password is here from inp
                    final EditText pwdInp = (EditText) pwdPromptView.findViewById(R.id.mpassword_prompt_edit_id);

                    pwdPromptBldr.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Check if the password salt hash is the same. Right -> continue; Else -> return
                            if (pwdInp.getText().toString().equals("ABC")) {
                                passSwch.setChecked(true);
                                vis.setVisibility(View.VISIBLE);
                            }
                            else {
                                passSwch.setChecked(false);
                                dialog.cancel();
                                Snackbar.make(root, "Incorrect Master Password/OTP! Try again", Snackbar.LENGTH_LONG).setAction("try again", null).show();
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
        return root;
    }


}
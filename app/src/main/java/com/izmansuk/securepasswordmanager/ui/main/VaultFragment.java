package com.izmansuk.securepasswordmanager.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.izmansuk.securepasswordmanager.R;

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
        vaultFrag.setArguments(bundle);
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
                Snackbar.make(v, "Your new password is safe!", Snackbar.LENGTH_LONG).setAction("Add action", null).show();
            }
        });
        passSwch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    vis.setVisibility(View.VISIBLE);
                }
                else {
                    vis.setVisibility(View.INVISIBLE);
                }
            }
        });
        return root;
    }
}
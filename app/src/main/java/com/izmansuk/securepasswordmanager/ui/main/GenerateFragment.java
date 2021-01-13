package com.izmansuk.securepasswordmanager.ui.main;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.ClipboardManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.izmansuk.securepasswordmanager.R;

/**
 * Generate password tab section fragment
 */
public class GenerateFragment extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private PageViewModel pageViewModel;

    public static GenerateFragment newInstance(int index) {
        GenerateFragment generateFrag = new GenerateFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        generateFrag.setArguments(bundle);
        return generateFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 2;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        textView.setText(R.string.generate_header);

        Button genButton = root.findViewById(R.id.generateButton);
        ImageButton copyButton = root.findViewById(R.id.copyButton);
        TextView passField = root.findViewById(R.id.editNewPassword);
        ClipboardManager clipBrd = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Generate strong password
                passField.setText("ThisIsYourNewPasswordLOL");
                Snackbar.make(v, "New password generated!", Snackbar.LENGTH_LONG).setAction("Generate action", null).show();
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passField.getText() != "" || passField.getText() == null) {
                    ClipData clipPass = ClipData.newPlainText("clipMes",passField.getText());
                    clipBrd.setPrimaryClip(clipPass);
                    Snackbar.make(v, "Copied!", Snackbar.LENGTH_SHORT).setAction("Copy action", null).show();
                }
            }
        });
        return root;
    }
}

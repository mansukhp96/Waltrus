package com.izmansuk.securepasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toolbar;

import java.io.NotActiveException;

public class AboutActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Toolbar support to navigate back home
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("About");
        }
        setContentView(R.layout.activity_about);

        String[] contactName = getResources().getStringArray(R.array.about_contact_name);
        ListView contactLst = (ListView) findViewById(R.id.about_list);
        ArrayAdapter arrAdp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, contactName);
        contactLst.setAdapter(arrAdp);
        contactLst.setOnItemClickListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] contactLnk = getResources().getStringArray(R.array.about_contact_values);
        if(position < contactLnk.length) {
            Uri lnk = Uri.parse(contactLnk[position]);
            if(contactLnk[position].equals("mansukhp96@gmail.com")) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:mansukhp96@gmail.com"));
                //startActivity(intent);
                startActivity(Intent.createChooser(emailIntent, "Send this Email via: "));
            }
            else {
                startActivity(new Intent(Intent.ACTION_VIEW, lnk));
            }
        }
    }
}
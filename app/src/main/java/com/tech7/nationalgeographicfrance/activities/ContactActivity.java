package com.tech7.nationalgeographicfrance.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tech7.nationalgeographicfrance.R;
import com.tech7.nationalgeographicfrance.utils.Tools;

public class ContactActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private String titleBar;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        initToolbar();

        email = findViewById(R.id.email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:tech7dev@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, "Envoyer un Message"));
            }
        });
    }
    private void initToolbar() {
        //setting title bar
        titleBar = "Contactez-nous!";
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(titleBar);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }
}

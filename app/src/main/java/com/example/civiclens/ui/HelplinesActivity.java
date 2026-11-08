package com.example.civiclens.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;

/**
 * National Emergency & Civic Helplines Hub
 */
public class HelplinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helplines);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnDial999).setOnClickListener(v -> IntentHelper.dial(this, "999"));
        findViewById(R.id.btnDial333).setOnClickListener(v -> IntentHelper.dial(this, "333"));
        findViewById(R.id.btnDial106).setOnClickListener(v -> IntentHelper.dial(this, "106"));
        findViewById(R.id.btnDial16263).setOnClickListener(v -> IntentHelper.dial(this, "16263"));
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

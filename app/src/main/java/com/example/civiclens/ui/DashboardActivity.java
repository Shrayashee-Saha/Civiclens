package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.example.civiclens.R;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton newsBtn = findViewById(R.id.dashNewsButton);
        if (newsBtn != null) {
            newsBtn.setOnClickListener(v -> startActivity(new Intent(this, NewsDashboardActivity.class)));
        }

        MaterialButton watchBtn = findViewById(R.id.dashWatchButton);
        if (watchBtn != null) {
            watchBtn.setOnClickListener(v -> startActivity(new Intent(this, AccountabilityActivity.class)));
        }

        androidx.recyclerview.widget.RecyclerView dashList = findViewById(R.id.dashCasesList);
        if (dashList != null) {
            dashList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            dashList.setNestedScrollingEnabled(false);
            AccountabilityAdapter adapter = new AccountabilityAdapter(url -> IntentHelper.openUrl(this, url));
            dashList.setAdapter(adapter);

            new com.example.civiclens.data.ProjectRepository(this).getAccountabilityCases(cases -> {
                if (isFinishing()) return;
                if (cases != null && cases.size() > 4) {
                    adapter.submit(cases.subList(0, 4));
                } else if (cases != null) {
                    adapter.submit(cases);
                }
            });
        }
    }
}

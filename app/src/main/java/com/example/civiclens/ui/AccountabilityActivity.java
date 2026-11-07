package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;

public class AccountabilityActivity extends AppCompatActivity {

    private ProjectRepository repository;
    private AccountabilityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountability);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        repository = new ProjectRepository(this);
        RecyclerView list = findViewById(R.id.casesList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountabilityAdapter(url -> IntentHelper.openUrl(this, url));
        list.setAdapter(adapter);

        View autoSurveyBtn = findViewById(R.id.btnAutoSurveyAccountability);
        if (autoSurveyBtn != null) {
            autoSurveyBtn.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Scanning live media & audit probes for graft & irregularities...", android.widget.Toast.LENGTH_SHORT).show();
                repository.runAccountabilityAutoSurvey(count -> {
                    if (isFinishing()) return;
                    if (count > 0) {
                        android.widget.Toast.makeText(this, "Auto-Survey Complete! Discovered " + count + " new investigation case(s).", android.widget.Toast.LENGTH_LONG).show();
                        loadCases();
                    } else {
                        android.widget.Toast.makeText(this, "Auto-Survey Complete! All media investigation records up to date.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        loadCases();
    }

    private void loadCases() {
        repository.getAccountabilityCases(cases -> adapter.submit(cases));
    }
}

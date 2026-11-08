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
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                android.widget.Toast.makeText(this, "Scanning live media & audit probes for graft & irregularities...", android.widget.Toast.LENGTH_SHORT).show();
                repository.runAccountabilityAutoSurvey(count -> {
                    if (isFinishing()) return;
                    if (count > 0) {
                        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "Auto-Survey Complete! Discovered " + count + " new investigation case(s).", false);
                        loadCases();
                    } else {
                        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "Auto-Survey Complete! All media investigation records up to date.", false);
                    }
                });
            });
        }

        View readVoiceBtn = findViewById(R.id.btnReadAccountabilityVoice);
        if (readVoiceBtn != null) {
            readVoiceBtn.setOnClickListener(v -> {
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                repository.getAccountabilityCases(cases -> {
                    if (cases == null || cases.isEmpty()) {
                        com.example.civiclens.accessibility.AccessibilityHelper.speak(this, "No active media investigation cases found.");
                        return;
                    }
                    StringBuilder sb = new StringBuilder("Accountability Watch Report. Total cases: ").append(cases.size()).append(". ");
                    for (int i = 0; i < Math.min(3, cases.size()); i++) {
                        com.example.civiclens.model.AccountabilityCase c = cases.get(i);
                        sb.append("Case ").append(i + 1).append(": ").append(c.title).append(". ");
                        sb.append("Status: ").append(c.investigationStatus).append(". ");
                        sb.append("Location: ").append(c.locationName).append(". ");
                    }
                    com.example.civiclens.accessibility.AccessibilityHelper.speak(this, sb.toString());
                });
            });
        }

        loadCases();
    }

    private void loadCases() {
        repository.getAccountabilityCases(cases -> adapter.submit(cases));
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

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

        MaterialButton pdfBtn = findViewById(R.id.btnExportDashPdf);
        if (pdfBtn != null) {
            pdfBtn.setOnClickListener(v -> {
                StringBuilder sb = new StringBuilder();
                sb.append("CIVICLENS BANGLADESH — EXECUTIVE INTELLIGENCE BRIEFING REPORT\n");
                sb.append("Generated: ").append(new java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale.US).format(new java.util.Date())).append("\n\n");
                sb.append("1. NATIONAL COVERAGE METRICS:\n");
                sb.append("   • National ADP Coverage: 78%\n");
                sb.append("   • Territorial Risk Status: Medium Risk\n");
                sb.append("   • Active Projects Monitored: 24 Public Infrastructure Projects\n\n");
                sb.append("2. KEY AUDIT FLAGS & TERRITORIAL RISKS:\n");
                sb.append("   • Shahjalal Airport Expansion — CAG Audit Flag (Tk 116.08 Million Irregularity)\n");
                sb.append("   • RAJUK Politically Allocated Plots (1,413 Plots Investigated)\n");
                sb.append("   • LGED Infrastructure Bridges — IMED Probe (Tk 343 Crore)\n");
                sb.append("   • Climate Fund Solar Streetlight Overpricing (Tk 21.10 Billion)\n\n");
                sb.append("Confidential Executive Document — CivicLens Public Intelligence");

                com.example.civiclens.accessibility.AccessibilityHelper.exportToPdf(this, "Executive_Intelligence_Briefing", sb.toString());
            });
        }

        MaterialButton voiceBtn = findViewById(R.id.btnReadDashVoice);
        if (voiceBtn != null) {
            voiceBtn.setOnClickListener(v -> {
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                String brief = "Executive Intelligence Briefing. National coverage 78 percent. 24 active projects monitored. Four key audit flags identified including Shahjalal Airport expansion CAG finding and RAJUK plot investigation.";
                com.example.civiclens.accessibility.AccessibilityHelper.speak(this, brief);
            });
        }
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

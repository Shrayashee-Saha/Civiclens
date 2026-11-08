package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;
import com.example.civiclens.data.ReportRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification Inbox & Case Tracker Screen
 */
public class NotificationsActivity extends AppCompatActivity {

    private LineAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        emptyText = findViewById(R.id.notificationsEmpty);
        RecyclerView list = findViewById(R.id.notificationsList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LineAdapter();
        list.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        List<LineAdapter.Line> lines = new ArrayList<>();

        // Add System & Project Alert Notifications
        lines.add(new LineAdapter.Line("🚨 System Alert: Low Battery Receiver Registered", "System Broadcast · Battery level below 15% notification trigger active"));
        lines.add(new LineAdapter.Line("📢 Project Alert: Dhaka Elevated Expressway", "Infrastructure Probe · CAG Audit finding Tk 116.08 Million Irregularity flagged"));
        lines.add(new LineAdapter.Line("⚡ Utility Alert: DESCO Smart Meter Sync", "Foreground Service · ObservationSyncService active with dataSync foreground service"));

        new ReportRepository(this).getAll(reports -> {
            if (reports != null) {
                for (com.example.civiclens.model.CitizenReport r : reports) {
                    lines.add(new LineAdapter.Line("📄 Report Tracked: " + r.category + " (" + r.district + ")", "Status: " + r.status + " · Route: " + (r.officeName != null ? r.officeName : "Direct Office Router")));
                }
            }
            adapter.submit(lines);
            emptyText.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

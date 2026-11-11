package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification Inbox & Case Tracker Screen
 */
public class NotificationsActivity extends AppCompatActivity {

    private InvitationAdapter adapter;
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
        toolbar.setNavigationOnClickListener(v -> IntentHelper.handleBackNavigation(this));
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                IntentHelper.handleBackNavigation(NotificationsActivity.this);
            }
        });

        emptyText = findViewById(R.id.notificationsEmpty);
        RecyclerView list = findViewById(R.id.notificationsList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvitationAdapter();
        list.setAdapter(adapter);

        View clearBtn = findViewById(R.id.btnClearInbox);
        if (clearBtn != null) {
            clearBtn.setOnClickListener(v -> {
                adapter.submit(new ArrayList<>());
                if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "🧹 Notification Inbox Cleared", false);
            });
        }

        loadNotifications();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadNotifications();
    }

    private void loadNotifications() {
        List<InvitationAdapter.InvitationItem> items = new ArrayList<>();

        android.content.Intent intent = getIntent();
        if (intent != null) {
            String actionStatus = intent.getStringExtra("action_status");
            String customTitle = intent.getStringExtra("notification_title");
            String customSender = intent.getStringExtra("notification_sender");

            if (customTitle != null && !customTitle.isEmpty()) {
                items.add(new InvitationAdapter.InvitationItem(
                        "inv_dyn_1",
                        customTitle,
                        customSender != null ? customSender : "Citizen Activist",
                        actionStatus != null ? actionStatus : "PENDING"
                ));
            } else if (actionStatus != null) {
                items.add(new InvitationAdapter.InvitationItem(
                        "inv_dyn_1",
                        "📩 Group Invitation: Environmental Protection Assembly",
                        "activist@civiclens.org",
                        actionStatus
                ));
                if ("ACCEPTED".equalsIgnoreCase(actionStatus)) {
                    com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "✓ Joined group successfully!", false);
                    startActivity(new android.content.Intent(this, CivicGroupActivity.class));
                }
            }
        }

        items.add(new InvitationAdapter.InvitationItem("inv_1", "📩 Group Invitation: Save the Sundarbans Alliance", "advocate.rahman@mopa.gov.bd", "PENDING"));
        items.add(new InvitationAdapter.InvitationItem("inv_2", "📩 Group Invitation: Protect Khulna Wetlands", "gazipur.activist@civiclens.org", "PENDING"));
        items.add(new InvitationAdapter.InvitationItem("inv_3", "📩 Group Invitation: Safe Roads & Infrastructure Assembly", "sylhet.engineer@rhd.gov.bd", "PENDING"));

        items.add(new InvitationAdapter.InvitationItem("sys_1", "🚨 System Alert: Low Battery Receiver Registered", "System Broadcast", "ACCEPTED"));
        items.add(new InvitationAdapter.InvitationItem("sys_2", "📢 Project Alert: Dhaka Elevated Expressway CAG Audit Flag", "CAG Audit Office", "ACCEPTED"));

        adapter.submit(items);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

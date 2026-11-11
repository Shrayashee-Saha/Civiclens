package com.example.civiclens.ui;

import android.content.Context;
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
    private com.google.firebase.firestore.ListenerRegistration invRegistration;

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
                getSharedPreferences("civiclens_notifications_history", MODE_PRIVATE).edit().clear().apply();
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
            }
        }

        try {
            android.content.SharedPreferences prefs = getSharedPreferences("civiclens_notifications_history", MODE_PRIVATE);
            String current = prefs.getString("history_json", "[]");
            org.json.JSONArray array = new org.json.JSONArray(current);
            for (int i = array.length() - 1; i >= 0; i--) {
                org.json.JSONObject obj = array.getJSONObject(i);
                String title = obj.optString("title", "📩 Civic Notification");
                String body = obj.optString("body", "");
                items.add(new InvitationAdapter.InvitationItem("inv_saved_" + i, title, body, "PENDING"));
            }
        } catch (Exception ignored) { }

        adapter.submit(items);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (com.example.civiclens.firebase.FirebaseGate.isReady(this)) {
            invRegistration = com.example.civiclens.cloud.GroupCloudRepository.listenForPendingInvitations(this, pendingList -> {
                if (pendingList != null && !pendingList.isEmpty()) {
                    List<InvitationAdapter.InvitationItem> cloudItems = new ArrayList<>();
                    for (com.example.civiclens.cloud.GroupCloudRepository.PendingInvitation inv : pendingList) {
                        cloudItems.add(new InvitationAdapter.InvitationItem(
                                inv.id,
                                "📩 Group Invitation: " + inv.groupName,
                                inv.senderName,
                                "PENDING"
                        ));
                    }
                    adapter.submit(cloudItems);
                    if (emptyText != null) emptyText.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (invRegistration != null) {
            invRegistration.remove();
            invRegistration = null;
        }
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

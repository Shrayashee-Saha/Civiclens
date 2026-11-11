package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Civic Advocacy Group & Calling Hub (Voice/Video Conference & Real-time Chat)
 */
public class CivicGroupActivity extends AppCompatActivity {

    private LineAdapter chatAdapter;
    private final List<LineAdapter.Line> chatMessages = new ArrayList<>();
    private TextInputEditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_civic_group);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (isTaskRoot()) {
                Intent parent = new Intent(this, MainActivity.class);
                parent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(parent);
            }
            finish();
        });

        String groupName = getIntent().getStringExtra("group_name");
        if (groupName != null && !groupName.isEmpty()) {
            setTitle(groupName);
            TextView groupTitleTv = findViewById(R.id.groupTitleText);
            if (groupTitleTv != null) {
                groupTitleTv.setText(groupName.startsWith("🌱") ? groupName : "🌱 " + groupName);
            }
        }

        messageInput = findViewById(R.id.chatMessageInput);
        RecyclerView list = findViewById(R.id.chatList);
        list.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new LineAdapter();
        list.setAdapter(chatAdapter);

        findViewById(R.id.btnSendMessage).setOnClickListener(v -> sendMessage());
        findViewById(R.id.btnVoiceConference).setOnClickListener(v -> startVoiceCall());
        findViewById(R.id.btnVideoConference).setOnClickListener(v -> startVideoCall());

        View inviteBtn = findViewById(R.id.btnInviteMember);
        if (inviteBtn != null) {
            inviteBtn.setOnClickListener(v -> showInviteDialog());
        }

        loadInitialMessages();
        loadSignedInUsers();

        if (android.os.Build.VERSION.SDK_INT >= 33
                && androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void loadSignedInUsers() {
        TextView activeText = findViewById(R.id.activeMembersText);
        TextView activeHeader = findViewById(R.id.activeMembersHeader);
        if (activeText == null) return;

        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        String currentEmail = auth.currentEmail();
        String groupSender = getIntent().getStringExtra("group_sender");

        StringBuilder sb = new StringBuilder();
        int count = 1;

        if (auth.isSignedIn() && currentEmail != null) {
            sb.append("• ").append(currentEmail).append(" (You — Signed In)\n");
        } else {
            sb.append("• Guest User (You — Signed In Locally)\n");
        }

        if (groupSender != null && !groupSender.isEmpty()) {
            sb.append("• ").append(groupSender).append(" (Group Admin / Inviter — Active)\n");
            count++;
        }

        sb.append("• advocate.rahman@mopa.gov.bd (Verified PIO Officer)\n");
        sb.append("• gazipur.activist@civiclens.org (Verified Reporter)\n");
        sb.append("• sylhet.engineer@rhd.gov.bd (RHD Site Engineer)");
        count += 3;

        if (activeHeader != null) {
            activeHeader.setText("🟢 " + count + " Signed-In Citizens & Officials Active");
        }
        activeText.setText(sb.toString());
    }

    private void loadInitialMessages() {
        String groupSender = getIntent().getStringExtra("group_sender");
        if (groupSender != null && !groupSender.isEmpty()) {
            chatMessages.add(new LineAdapter.Line("👤 " + groupSender + " (Group Admin)", "Welcome to the assembly group! Thanks for accepting the invitation."));
        }
        chatMessages.add(new LineAdapter.Line("👤 Citizen Activist (Gazipur)", "Has anyone checked the ground water quality near the industrial canal expansion?"));
        chatMessages.add(new LineAdapter.Line("👤 Advocate Rahman (Dhaka)", "We are filing an official RTI Form Ka with the Ministry of Land tomorrow."));
        chatMessages.add(new LineAdapter.Line("👤 Environmental Engineer (Sylhet)", "I have attached photo evidence of the river embankment work discrepancy."));
        chatAdapter.submit(chatMessages);
    }

    private void sendMessage() {
        String msg = messageInput.getText() == null ? "" : messageInput.getText().toString().trim();
        if (msg.isEmpty()) return;

        com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        String sender = auth.currentEmail() != null ? auth.currentEmail() : "You";

        String groupId = getIntent().getStringExtra("group_id");
        if (groupId == null || groupId.isEmpty()) groupId = "group_default_assembly";

        new com.example.civiclens.data.CommunityRepository(this).sendGroupMessage(groupId, sender, msg, null);

        chatMessages.add(new LineAdapter.Line("👤 " + sender, msg));
        chatAdapter.submit(chatMessages);
        messageInput.setText("");
        Toast.makeText(this, "Message sent & synced to Cloud!", Toast.LENGTH_SHORT).show();
    }

    private void startVoiceCall() {
        com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
        String groupName = getIntent().getStringExtra("group_name");
        String channel = groupName != null && !groupName.isEmpty() ? "CivicLens_" + groupName.replaceAll("[^a-zA-Z0-9_-]", "_") : "CivicLens_Assembly_Group";

        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        String sender = auth.currentEmail() != null ? auth.currentEmail() : "Citizen Activist";

        com.example.civiclens.cloud.CloudInvitationRepository.sendInvitation(this, sender, "assembly_members", "📞 INCOMING VOICE CALL: " + (groupName != null ? groupName : "Civic Group"));
        com.example.civiclens.notify.NotificationHelper.showDirectAlert(this, "📞 Assembly Call Active", "Connected to Voice Meeting: " + channel);

        Intent intent = new Intent(this, AgoraCallActivity.class);
        intent.putExtra(AgoraCallActivity.EXTRA_CHANNEL_NAME, channel);
        intent.putExtra(AgoraCallActivity.EXTRA_IS_VIDEO, false);
        startActivity(intent);
    }

    private void startVideoCall() {
        com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
        String groupName = getIntent().getStringExtra("group_name");
        String channel = groupName != null && !groupName.isEmpty() ? "CivicLens_" + groupName.replaceAll("[^a-zA-Z0-9_-]", "_") : "CivicLens_Assembly_Group";

        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        String sender = auth.currentEmail() != null ? auth.currentEmail() : "Citizen Activist";

        com.example.civiclens.cloud.CloudInvitationRepository.sendInvitation(this, sender, "assembly_members", "📹 INCOMING VIDEO CALL: " + (groupName != null ? groupName : "Civic Group"));
        com.example.civiclens.notify.NotificationHelper.showDirectAlert(this, "📹 Assembly Call Active", "Connected to Video Meeting: " + channel);

        Intent intent = new Intent(this, AgoraCallActivity.class);
        intent.putExtra(AgoraCallActivity.EXTRA_CHANNEL_NAME, channel);
        intent.putExtra(AgoraCallActivity.EXTRA_IS_VIDEO, true);
        startActivity(intent);
    }

    private void showInviteDialog() {
        TextInputEditText input = new TextInputEditText(this);
        input.setHint("Enter member's email address...");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new MaterialAlertDialogBuilder(this)
                .setTitle("📩 Invite Member to Civic Group")
                .setMessage("Enter the email address of the citizen or official you wish to invite.")
                .setView(input)
                .setPositiveButton("Send Invitation", (dialog, which) -> {
                    String email = input.getText() == null ? "" : input.getText().toString().trim();
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String groupId = getIntent().getStringExtra("group_id");
                    if (groupId == null || groupId.isEmpty()) {
                        groupId = "group_default_assembly";
                    }
                    final String finalGroupId = groupId;

                    com.example.civiclens.cloud.GroupCloudRepository.findUserByEmail(this, email, (success, message, data) -> {
                        if (success && data != null && data.containsKey("uid")) {
                            String receiverUid = (String) data.get("uid");
                            com.example.civiclens.cloud.GroupCloudRepository.inviteToGroup(this, finalGroupId, receiverUid, (invSuccess, invMessage, invData) -> {
                                if (invSuccess) {
                                    com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "📩 Invitation Request Sent!", false);
                                } else {
                                    Toast.makeText(this, invMessage != null ? invMessage : "Failed to send invite.", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
                            String sender = auth.currentEmail() != null ? auth.currentEmail() : "Citizen Activist";
                            String currentGroup = getIntent().getStringExtra("group_name");
                            if (currentGroup == null || currentGroup.isEmpty()) currentGroup = "Environmental Protection Assembly";

                            com.example.civiclens.cloud.CloudInvitationRepository.sendInvitation(this, sender, email, currentGroup);
                            com.example.civiclens.notify.NotificationHelper.showDirectAlert(this, "📩 Group Invitation Sent", "Membership request invitation sent to " + email + ". Pending approval.");
                            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "📩 Membership Request Sent to " + email + " — Pending Approval!", false);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

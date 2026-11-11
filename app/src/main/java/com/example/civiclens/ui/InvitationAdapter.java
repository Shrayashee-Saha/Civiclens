package com.example.civiclens.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.civiclens.R;
import com.example.civiclens.cloud.CloudInvitationRepository;

import java.util.ArrayList;
import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    public static class InvitationItem {
        final String invId;
        final String title;
        final String sender;
        final String groupId;
        String status;

        public InvitationItem(String invId, String title, String sender, String status, String groupId) {
            this.invId = invId;
            this.title = title;
            this.sender = sender;
            this.status = status;
            this.groupId = groupId != null ? groupId : "";
        }

        public InvitationItem(String invId, String title, String sender, String status) {
            this(invId, title, sender, status, "");
        }
    }

    private final List<InvitationItem> items = new ArrayList<>();

    public void submit(List<InvitationItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvitationItem item = items.get(position);
        holder.title.setText(item.title);
        holder.sender.setText("From: " + item.sender + " · Status: " + item.status);

        if ("ACCEPTED".equalsIgnoreCase(item.status)) {
            holder.buttonsLayout.setVisibility(View.GONE);
            holder.sender.setText("From: " + item.sender + " · Status: ACCEPTED ✓");
        } else if ("DECLINED".equalsIgnoreCase(item.status)) {
            holder.buttonsLayout.setVisibility(View.GONE);
            holder.sender.setText("From: " + item.sender + " · Status: DECLINED ✕");
        } else {
            holder.buttonsLayout.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(v -> {
            Context ctx = v.getContext();
            String groupName = parseGroupName(item.title, item.sender);
            String targetGroupId = item.groupId != null && !item.groupId.isEmpty() ? item.groupId : "group_" + groupName.toLowerCase().replaceAll("[^a-z0-9]", "_");

            item.status = "ACCEPTED";
            notifyDataSetChanged();

            com.example.civiclens.data.CommunityRepository repo = new com.example.civiclens.data.CommunityRepository(ctx);
            repo.joinCommunityByName(groupName, null);
            repo.registerCloudMembership(targetGroupId, groupName, null);

            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(ctx, "✓ Joined " + groupName + " successfully!", false);

            if (com.example.civiclens.firebase.FirebaseGate.isReady(ctx)) {
                com.example.civiclens.cloud.GroupCloudRepository.respondToInvitation(ctx, item.invId, true, (ok, msg, data) -> {
                    if (!ok) {
                        CloudInvitationRepository.respondToFirestoreInvitation(ctx, item.invId, "ACCEPTED", null);
                    }
                });
            }

            Intent i = new Intent(ctx, CivicGroupActivity.class);
            i.putExtra("group_id", targetGroupId);
            i.putExtra("group_name", groupName);
            i.putExtra("group_sender", item.sender);
            ctx.startActivity(i);
        });

        holder.btnDecline.setOnClickListener(v -> {
            Context ctx = v.getContext();
            item.status = "DECLINED";
            notifyDataSetChanged();
            if (com.example.civiclens.firebase.FirebaseGate.isReady(ctx)) {
                com.example.civiclens.cloud.GroupCloudRepository.respondToInvitation(ctx, item.invId, false, (ok, msg, data) -> {
                    if (!ok) {
                        CloudInvitationRepository.respondToFirestoreInvitation(ctx, item.invId, "DECLINED", null);
                    }
                });
            }
        });
    }

    private static String parseGroupName(String title, String sender) {
        if (title == null) return "Save the Sundarbans & Coastal Forests";
        String clean = title;
        clean = clean.replace("📩 Group Invitation:", "");
        clean = clean.replace("📩 New Group Invitation", "");
        clean = clean.replace("📩 New Cross-Device Invitation", "");
        clean = clean.replace("📩 Group Invitation Sent", "");
        clean = clean.trim();

        if (clean.startsWith(":")) {
            clean = clean.substring(1).trim();
        }

        if ((clean.isEmpty() || "📩 New Group Invitation".contains(clean)) && sender != null && sender.contains("for: ")) {
            clean = sender.substring(sender.indexOf("for: ") + 5).trim();
        }

        if (clean.isEmpty() || "📩 New Group Invitation".contains(clean)) {
            clean = "Save the Sundarbans & Coastal Forests";
        }
        return clean;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView sender;
        final View buttonsLayout;
        final MaterialButton btnAccept;
        final MaterialButton btnDecline;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.invitationTitle);
            sender = itemView.findViewById(R.id.invitationSender);
            buttonsLayout = itemView.findViewById(R.id.invitationButtonsLayout);
            btnAccept = itemView.findViewById(R.id.btnAcceptInvitation);
            btnDecline = itemView.findViewById(R.id.btnDeclineInvitation);
        }
    }
}

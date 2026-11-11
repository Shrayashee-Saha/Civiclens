package com.example.civiclens.ui;

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
        String status;

        public InvitationItem(String invId, String title, String sender, String status) {
            this.invId = invId;
            this.title = title;
            this.sender = sender;
            this.status = status;
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
            item.status = "ACCEPTED";
            holder.buttonsLayout.setVisibility(View.GONE);
            holder.sender.setText("From: " + item.sender + " · Status: ACCEPTED ✓");

            String groupName = parseGroupName(item.title, item.sender);

            new com.example.civiclens.data.CommunityRepository(v.getContext()).joinCommunityByName(groupName, null);

            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(v.getContext(), "✓ Joined " + groupName + " successfully!", false);

            CloudInvitationRepository.respondToFirestoreInvitation(v.getContext(), item.invId, "ACCEPTED", null);

            android.content.Intent intent = new android.content.Intent(v.getContext(), CivicGroupActivity.class);
            intent.putExtra("group_name", groupName);
            intent.putExtra("group_sender", item.sender);
            v.getContext().startActivity(intent);
        });

        holder.btnDecline.setOnClickListener(v -> {
            item.status = "DECLINED";
            holder.buttonsLayout.setVisibility(View.GONE);
            holder.sender.setText("From: " + item.sender + " · Status: DECLINED ✕");
            CloudInvitationRepository.respondToFirestoreInvitation(v.getContext(), item.invId, "DECLINED", () -> {
                Toast.makeText(v.getContext(), "Invitation declined.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private static String parseGroupName(String title, String sender) {
        if (title == null) return "Save the Sundarbans & Coastal Forests";
        String clean = title;
        clean = clean.replace("📩 Group Invitation:", "");
        clean = clean.replace("📩 New Cross-Device Invitation", "");
        clean = clean.replace("📩 Group Invitation Sent", "");
        clean = clean.trim();
        if (clean.isEmpty() && sender != null && sender.contains("for: ")) {
            clean = sender.substring(sender.indexOf("for: ") + 5).trim();
        }
        if (clean.isEmpty()) {
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

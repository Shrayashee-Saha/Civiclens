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
import com.example.civiclens.model.CivicPost;

import java.util.ArrayList;
import java.util.List;

public class CivicPostAdapter extends RecyclerView.Adapter<CivicPostAdapter.ViewHolder> {

    private final List<CivicPost> items = new ArrayList<>();

    public void submit(List<CivicPost> posts) {
        items.clear();
        items.addAll(posts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_civic_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CivicPost post = items.get(position);
        holder.author.setText(post.authorName);
        holder.category.setText(post.category);
        holder.meta.setText(post.district + " · 2 hours ago");
        holder.content.setText(post.content);

        holder.btnSupport.setText("👍 Support (" + post.supportCount + ")");
        holder.btnComment.setText("💬 Comment (" + post.commentCount + ")");
        holder.btnShare.setText("🔄 Share (" + post.shareCount + ")");

        holder.btnSupport.setOnClickListener(v -> {
            post.supportCount++;
            holder.btnSupport.setText("👍 Support (" + post.supportCount + ")");
            com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(v.getContext());
            Toast.makeText(v.getContext(), "👍 You supported this civic post!", Toast.LENGTH_SHORT).show();
        });

        holder.btnComment.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Opening comments thread...", Toast.LENGTH_SHORT).show());

        holder.btnShare.setOnClickListener(v ->
                IntentHelper.shareText(v.getContext(), post.content, "Share Civic Post"));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView category;
        final TextView meta;
        final TextView content;
        final MaterialButton btnSupport;
        final MaterialButton btnComment;
        final MaterialButton btnShare;

        ViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.postAuthor);
            category = itemView.findViewById(R.id.postCategoryChip);
            meta = itemView.findViewById(R.id.postMeta);
            content = itemView.findViewById(R.id.postContent);
            btnSupport = itemView.findViewById(R.id.btnSupport);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}

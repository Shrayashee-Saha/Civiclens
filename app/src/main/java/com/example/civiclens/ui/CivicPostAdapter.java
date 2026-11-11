package com.example.civiclens.ui;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.civiclens.R;
import com.example.civiclens.data.CommunityRepository;
import com.example.civiclens.model.CivicPost;

import java.util.ArrayList;
import java.util.List;

public class CivicPostAdapter extends RecyclerView.Adapter<CivicPostAdapter.ViewHolder> {

    private final List<CivicPost> items = new ArrayList<>();

    public void submit(List<CivicPost> posts) {
        items.clear();
        if (posts != null) {
            items.addAll(posts);
        }
        notifyDataSetChanged();
    }

    public void addPostAtTop(CivicPost post) {
        if (post == null) return;
        items.add(0, post);
        notifyItemInserted(0);
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

        if (post.mediaUrl != null && !post.mediaUrl.trim().isEmpty()) {
            try {
                Uri mediaUri = Uri.parse(post.mediaUrl);
                Context ctx = holder.itemView.getContext();
                try {
                    ctx.getContentResolver().takePersistableUriPermission(mediaUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Throwable ignored) { }

                java.io.InputStream stream = ctx.getContentResolver().openInputStream(mediaUri);
                if (stream != null) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(stream);
                    stream.close();
                    if (bitmap != null) {
                        holder.postImage.setImageBitmap(bitmap);
                        holder.postImage.setVisibility(View.VISIBLE);
                    } else {
                        holder.postImage.setImageDrawable(null);
                        holder.postImage.setVisibility(View.GONE);
                    }
                } else {
                    holder.postImage.setImageDrawable(null);
                    holder.postImage.setVisibility(View.GONE);
                }
            } catch (Throwable e) {
                holder.postImage.setImageDrawable(null);
                holder.postImage.setVisibility(View.GONE);
            }
        } else {
            holder.postImage.setImageDrawable(null);
            holder.postImage.setVisibility(View.GONE);
        }

        holder.btnSupport.setText("👍 Support (" + post.supportCount + ")");
        holder.btnComment.setText("💬 Comment (" + post.commentCount + ")");
        holder.btnShare.setText("🔄 Share (" + post.shareCount + ")");

        holder.btnSupport.setOnClickListener(v -> {
            post.supportCount++;
            holder.btnSupport.setText("👍 Support (" + post.supportCount + ")");
            com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(v.getContext());
            new CommunityRepository(v.getContext()).supportPost(post.id, null);
            Toast.makeText(v.getContext(), "👍 You supported this civic post!", Toast.LENGTH_SHORT).show();
        });

        holder.btnComment.setOnClickListener(v -> showCommentsDialog(v.getContext(), post, holder));

        holder.btnShare.setOnClickListener(v ->
                IntentHelper.shareText(v.getContext(), post.content, "Share Civic Post"));
    }

    private void showCommentsDialog(Context context, CivicPost post, ViewHolder holder) {
        com.google.android.material.textfield.TextInputEditText input =
                new com.google.android.material.textfield.TextInputEditText(context);
        input.setHint("Write a civic comment...");

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                .setTitle("💬 Comments — " + post.authorName)
                .setMessage("Post: " + post.content + "\n\n• Citizen Activist: Verified ground report for this location.")
                .setView(input)
                .setPositiveButton("Post Comment", (dialog, which) -> {
                    String comment = input.getText() == null ? "" : input.getText().toString().trim();
                    if (!comment.isEmpty()) {
                        post.commentCount++;
                        holder.btnComment.setText("💬 Comment (" + post.commentCount + ")");
                        new CommunityRepository(context).addCommentToPost(post.id, comment, null);
                        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(context, "💬 Comment posted successfully!", false);
                    }
                })
                .setNegativeButton("Close", null)
                .show();
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
        final ImageView postImage;
        final MaterialButton btnSupport;
        final MaterialButton btnComment;
        final MaterialButton btnShare;

        ViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.postAuthor);
            category = itemView.findViewById(R.id.postCategoryChip);
            meta = itemView.findViewById(R.id.postMeta);
            content = itemView.findViewById(R.id.postContent);
            postImage = itemView.findViewById(R.id.postImage);
            btnSupport = itemView.findViewById(R.id.btnSupport);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}

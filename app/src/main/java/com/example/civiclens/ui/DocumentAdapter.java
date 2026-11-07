package com.example.civiclens.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.civiclens.R;
import com.example.civiclens.model.ServiceDocument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    public interface OnToggle {
        void onToggle(String documentId, boolean checked);
    }

    private final OnToggle listener;
    private List<ServiceDocument> documents = new ArrayList<>();
    private Set<String> collected = new HashSet<>();

    public DocumentAdapter(OnToggle listener) {
        this.listener = listener;
    }

    public void submit(List<ServiceDocument> docs, Set<String> collectedIds) {
        this.documents = docs;
        this.collected = collectedIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceDocument doc = documents.get(position);

        // Clear the old listener first so recycled rows do not fire stale callbacks.
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(collected.contains(doc.id));
        holder.title.setText(doc.title);
        holder.badge.setText(doc.mandatory
                ? R.string.required_label : R.string.conditional_label);
        holder.note.setText(doc.note == null ? "" : doc.note);
        holder.note.setVisibility(doc.note == null || doc.note.isEmpty()
                ? View.GONE : View.VISIBLE);

        holder.checkBox.setOnCheckedChangeListener((button, checked) ->
                listener.onToggle(doc.id, checked));
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView title;
        final TextView badge;
        final TextView note;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.docCheck);
            title = itemView.findViewById(R.id.docTitle);
            badge = itemView.findViewById(R.id.docBadge);
            note = itemView.findViewById(R.id.docNote);
        }
    }
}

package com.example.civiclens.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.civiclens.R;

import java.util.ArrayList;
import java.util.List;

/** A simple two-line row, used for projects, milestones, observations, and the audit trail. */
public class LineAdapter extends RecyclerView.Adapter<LineAdapter.ViewHolder> {

    public interface OnLineClick {
        void onClick(int position);
    }

    public static final class Line {
        public final String title;
        public final String subtitle;

        public Line(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private final OnLineClick listener;
    private final List<Line> items = new ArrayList<>();

    public LineAdapter() {
        this(null);
    }

    public LineAdapter(OnLineClick listener) {
        this.listener = listener;
    }

    public void submit(List<Line> lines) {
        items.clear();
        items.addAll(lines);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Line line = items.get(position);
        holder.title.setText(line.title);
        holder.subtitle.setText(line.subtitle);
        holder.subtitle.setVisibility(line.subtitle == null || line.subtitle.isEmpty()
                ? View.GONE : View.VISIBLE);

        if (listener != null) {
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> listener.onClick(holder.getBindingAdapterPosition()));
        } else {
            holder.itemView.setClickable(false);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lineTitle);
            subtitle = itemView.findViewById(R.id.lineSubtitle);
        }
    }
}

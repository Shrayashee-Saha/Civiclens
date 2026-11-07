package com.example.civiclens.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.civiclens.R;
import com.example.civiclens.model.GovernmentService;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    public interface OnServiceClick {
        void onClick(GovernmentService service);
    }

    private final OnServiceClick listener;
    private final List<GovernmentService> items = new ArrayList<>();

    public ServiceAdapter(OnServiceClick listener) {
        this.listener = listener;
    }

    public void submit(List<GovernmentService> services) {
        items.clear();
        items.addAll(services);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GovernmentService service = items.get(position);
        holder.name.setText(service.nameEn);
        holder.category.setText(service.category);
        holder.authority.setText(service.authority);

        if (service.demoData) {
            holder.trustTag.setText(R.string.demo_tag);
            holder.trustTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning));
        } else if (service.lastVerified != null && !service.lastVerified.equals("NOT VERIFIED")) {
            holder.trustTag.setText(holder.itemView.getContext().getString(R.string.verified_tag, service.lastVerified));
            holder.trustTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        } else {
            holder.trustTag.setText(R.string.needs_checking_tag);
            holder.trustTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning));
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(service));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView category;
        final TextView authority;
        final TextView trustTag;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.serviceName);
            category = itemView.findViewById(R.id.serviceCategory);
            authority = itemView.findViewById(R.id.serviceAuthority);
            trustTag = itemView.findViewById(R.id.serviceTrustTag);
        }
    }
}

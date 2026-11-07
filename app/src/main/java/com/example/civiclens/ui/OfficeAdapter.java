package com.example.civiclens.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.example.civiclens.R;
import com.example.civiclens.model.GovernmentOffice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OfficeAdapter extends RecyclerView.Adapter<OfficeAdapter.ViewHolder> {

    public interface Actions {
        void onNavigate(GovernmentOffice office);
        void onCall(GovernmentOffice office);
        void onMessage(GovernmentOffice office);
        void onToggleSave(GovernmentOffice office, boolean save);
    }

    private final Actions actions;
    private final List<GovernmentOffice> items = new ArrayList<>();
    private Set<String> savedIds = new HashSet<>();

    public OfficeAdapter(Actions actions) {
        this.actions = actions;
    }

    public void submit(List<GovernmentOffice> offices, Set<String> saved) {
        items.clear();
        items.addAll(offices);
        savedIds = saved;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_office, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GovernmentOffice office = items.get(position);

        holder.name.setText(office.nameEn);
        holder.type.setText(typeLabel(office.type));
        holder.location.setText(location(office));
        holder.hours.setText(holder.itemView.getContext()
                .getString(R.string.hours_format, office.openingHours));

        if (office.demoData) {
            holder.demo.setVisibility(View.VISIBLE);
            holder.demo.setText(R.string.demo_tag);
            holder.demo.setBackgroundResource(R.drawable.bg_warning_chip);
            holder.demo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning));
        } else if (office.lastVerified != null && !office.lastVerified.equals("NOT VERIFIED")) {
            holder.demo.setVisibility(View.VISIBLE);
            holder.demo.setText(holder.itemView.getContext().getString(R.string.verified_tag, office.lastVerified));
            holder.demo.setBackgroundResource(R.drawable.bg_verified_chip);
            holder.demo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        } else {
            holder.demo.setVisibility(View.VISIBLE);
            holder.demo.setText(R.string.needs_checking_tag);
            holder.demo.setBackgroundResource(R.drawable.bg_warning_chip);
            holder.demo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning));
        }

        holder.navigate.setOnClickListener(v -> actions.onNavigate(office));

        // Call and Message only appear when a verified phone number is stored.
        boolean hasPhone = !TextUtils.isEmpty(office.phone);
        holder.call.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
        holder.message.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
        if (hasPhone) {
            holder.call.setOnClickListener(v -> actions.onCall(office));
            holder.message.setOnClickListener(v -> actions.onMessage(office));
        }

        boolean saved = savedIds.contains(office.id);
        holder.save.setText(saved ? R.string.action_saved : R.string.action_save);
        holder.save.setOnClickListener(v -> actions.onToggleSave(office, !saved));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static int typeLabel(String type) {
        if (GovernmentOffice.TYPE_AUTHORIZED.equals(type)) {
            return R.string.type_authorized;
        }
        if (GovernmentOffice.TYPE_GENERAL.equals(type)) {
            return R.string.type_general;
        }
        return R.string.type_assistance;
    }

    private static String location(GovernmentOffice o) {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{o.unionName, o.upazila, o.district}) {
            if (part != null && !part.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView type;
        final TextView location;
        final TextView hours;
        final TextView demo;
        final MaterialButton navigate;
        final MaterialButton call;
        final MaterialButton message;
        final MaterialButton save;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.officeName);
            type = itemView.findViewById(R.id.officeType);
            location = itemView.findViewById(R.id.officeLocation);
            hours = itemView.findViewById(R.id.officeHours);
            demo = itemView.findViewById(R.id.officeDemoTag);
            navigate = itemView.findViewById(R.id.navigateButton);
            call = itemView.findViewById(R.id.callButton);
            message = itemView.findViewById(R.id.messageButton);
            save = itemView.findViewById(R.id.saveOfficeButton);
        }
    }
}

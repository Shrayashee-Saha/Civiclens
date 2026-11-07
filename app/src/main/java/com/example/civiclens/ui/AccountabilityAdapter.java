package com.example.civiclens.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.civiclens.R;
import com.example.civiclens.model.AccountabilityCase;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AccountabilityAdapter extends RecyclerView.Adapter<AccountabilityAdapter.ViewHolder> {

    public interface OnReadArticle {
        void onRead(String url);
    }

    private final OnReadArticle listener;
    private final List<AccountabilityCase> items = new ArrayList<>();

    public AccountabilityAdapter(OnReadArticle listener) {
        this.listener = listener;
    }

    public void submit(List<AccountabilityCase> cases) {
        items.clear();
        items.addAll(cases);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accountability_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountabilityCase item = items.get(position);
        holder.title.setText(com.example.civiclens.service.NewsFetcher.cleanPlainText(item.title));
        holder.publisherDate.setText(item.publisher + " · " + item.publicationDate);
        holder.concern.setText(com.example.civiclens.service.NewsFetcher.cleanPlainText(item.reportedConcern));
        holder.authority.setText("Authority: " + item.authority);
        holder.disclaimer.setText("Source Status: " + item.sourceStatus);

        if (!TextUtils.isEmpty(item.locationName) || (item.latitude != 0.0 && item.longitude != 0.0)) {
            holder.gpsBadge.setVisibility(View.VISIBLE);
            String loc = TextUtils.isEmpty(item.locationName) ? "Bangladesh" : item.locationName;
            holder.gpsBadge.setText(String.format(Locale.US, "📍 GPS: %.4f° N, %.4f° E · %s", item.latitude, item.longitude, loc));
        } else {
            holder.gpsBadge.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.imageUrl)) {
            holder.image.setVisibility(View.VISIBLE);
            loadImageAsync(item.imageUrl, holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        if (item.financialImpactBdt > 0) {
            holder.impact.setVisibility(View.VISIBLE);
            double crore = item.financialImpactBdt / 10000000.0;
            holder.impact.setText(String.format(Locale.US, "Reported Financial Impact: ৳ %.2f Crore", crore));
        } else {
            holder.impact.setVisibility(View.GONE);
        }

        holder.statusTag.setText(item.investigationStatus);
        if ("Finding Established".equals(item.investigationStatus) || "Case Filed".equals(item.investigationStatus)) {
            holder.statusTag.setBackgroundResource(R.drawable.bg_warning_chip);
            holder.statusTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_red));
        } else if ("ACC Inquiry".equals(item.investigationStatus)) {
            holder.statusTag.setBackgroundResource(R.drawable.bg_warning_chip);
            holder.statusTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.warning));
        } else {
            holder.statusTag.setBackgroundResource(R.drawable.bg_verified_chip);
            holder.statusTag.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        }

        holder.readButton.setOnClickListener(v -> listener.onRead(item.newsUrl));
    }

    private void loadImageAsync(String imageUrl, ImageView imageView) {
        imageView.setTag(imageUrl);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    InputStream in = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    if (bitmap != null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (imageUrl.equals(imageView.getTag())) {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
                conn.disconnect();
            } catch (Exception ignored) { }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView gpsBadge;
        final TextView statusTag;
        final TextView publisherDate;
        final TextView title;
        final TextView concern;
        final TextView impact;
        final TextView authority;
        final TextView disclaimer;
        final MaterialButton readButton;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.caseImage);
            gpsBadge = itemView.findViewById(R.id.caseGpsBadge);
            statusTag = itemView.findViewById(R.id.caseStatusTag);
            publisherDate = itemView.findViewById(R.id.casePublisherDate);
            title = itemView.findViewById(R.id.caseTitle);
            concern = itemView.findViewById(R.id.caseConcern);
            impact = itemView.findViewById(R.id.caseImpact);
            authority = itemView.findViewById(R.id.caseAuthority);
            disclaimer = itemView.findViewById(R.id.caseSourceDisclaimer);
            readButton = itemView.findViewById(R.id.readArticleButton);
        }
    }
}

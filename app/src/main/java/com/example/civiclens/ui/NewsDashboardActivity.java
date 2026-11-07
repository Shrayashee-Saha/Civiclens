package com.example.civiclens.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;
import com.example.civiclens.model.AccountabilityCase;
import com.example.civiclens.service.NewsFetcher;

import java.util.ArrayList;
import java.util.List;

public class NewsDashboardActivity extends AppCompatActivity {

    private ProjectRepository repository;
    private AccountabilityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        repository = new ProjectRepository(this);
        RecyclerView list = findViewById(R.id.newsList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountabilityAdapter(url -> IntentHelper.openUrl(this, url));
        list.setAdapter(adapter);

        repository.getAccountabilityCases(cases -> {
            List<AccountabilityCase> combined = new ArrayList<>(cases);
            adapter.submit(combined);

            NewsFetcher.fetchAll(liveNews -> {
                if (liveNews != null && !liveNews.isEmpty()) {
                    combined.addAll(liveNews);
                    adapter.submit(combined);
                }
            });
        });
    }
}

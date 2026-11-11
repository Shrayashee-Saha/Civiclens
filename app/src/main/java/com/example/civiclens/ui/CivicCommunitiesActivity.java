package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;
import com.example.civiclens.data.CommunityRepository;
import com.example.civiclens.model.CivicCommunity;

import java.util.ArrayList;
import java.util.List;

public class CivicCommunitiesActivity extends AppCompatActivity {

    private LineAdapter adapter;
    private CommunityRepository repository;
    private List<CivicCommunity> currentGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_civic_communities);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> IntentHelper.handleBackNavigation(this));
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                IntentHelper.handleBackNavigation(CivicCommunitiesActivity.this);
            }
        });

        repository = new CommunityRepository(this);

        RecyclerView list = findViewById(R.id.communityList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LineAdapter(position -> {
            if (position >= 0 && position < currentGroups.size()) {
                CivicCommunity selected = currentGroups.get(position);
                Intent intent = new Intent(this, CivicGroupActivity.class);
                intent.putExtra("group_name", selected.name);
                intent.putExtra("group_desc", selected.description);
                intent.putExtra("group_district", selected.district);
                startActivity(intent);
            }
        });
        list.setAdapter(adapter);

        findViewById(R.id.btnCreateGroup).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGroupActivity.class)));

        loadCommunities();
    }

    private void loadCommunities() {
        repository.seedIfEmpty(() -> {
            repository.getAllCommunities(groups -> {
                if (isFinishing()) return;
                currentGroups = groups;
                List<LineAdapter.Line> lines = new ArrayList<>();
                for (CivicCommunity c : groups) {
                    String badge = c.isMember ? " (✓ Joined Member)" : "";
                    lines.add(new LineAdapter.Line("🌱 " + c.name + badge, c.communityType + " · " + c.district + " · " + c.memberCount + " Members"));
                }
                adapter.submit(lines);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCommunities();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            repository.startCommunitySync(uid);
            repository.startPostSync();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        repository.stopAllSync();
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

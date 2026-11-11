package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;
import com.example.civiclens.data.CommunityRepository;

/**
 * Serious Civic Community Media Feed (Photos, Videos, Ground Reports)
 */
public class CivicFeedActivity extends AppCompatActivity {

    private CivicPostAdapter adapter;
    private CommunityRepository repository;
    private TextInputEditText postInput;
    private String selectedCategory = "ALL";
    private String attachedMediaUri = "";

    private final ActivityResultLauncher<String[]> pickMedia = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                attachedMediaUri = uri.toString();
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "📷 Photo / Video Evidence attached to post!", false);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_civic_feed);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (isTaskRoot()) {
                Intent parent = new Intent(this, MainActivity.class);
                parent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(parent);
            }
            finish();
        });

        repository = new CommunityRepository(this);
        repository.seedIfEmpty(null);

        postInput = findViewById(R.id.feedPostInput);
        RecyclerView list = findViewById(R.id.feedList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CivicPostAdapter();
        list.setAdapter(adapter);

        MaterialButton attachBtn = findViewById(R.id.btnAttachMedia);
        if (attachBtn != null) {
            attachBtn.setOnClickListener(v -> pickMedia.launch(new String[]{"image/*", "video/*"}));
        }

        MaterialButton publishBtn = findViewById(R.id.btnPublishPost);
        if (publishBtn != null) {
            publishBtn.setOnClickListener(v -> publishPost());
        }

        bindFilter(R.id.filterAll, "ALL");
        bindFilter(R.id.filterEnvironment, "Environment");
        bindFilter(R.id.filterInfrastructure, "Infrastructure");

        View commBtn = findViewById(R.id.filterCommunities);
        if (commBtn != null) {
            commBtn.setOnClickListener(v -> startActivity(new Intent(this, CivicCommunitiesActivity.class)));
        }

        loadFeed();
    }

    private void bindFilter(int resId, String category) {
        View btn = findViewById(resId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                selectedCategory = category;
                loadFeed();
            });
        }
    }

    private void publishPost() {
        String content = postInput.getText() == null ? "" : postInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter a public concern or update to publish.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        String author = auth.currentEmail() != null ? auth.currentEmail() : "Citizen Activist";

        repository.createPost(author, "Environment", "Khulna", "text", content, attachedMediaUri, () -> {
            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "🌱 Civic Post Published to Feed!", false);
            postInput.setText("");
            attachedMediaUri = "";
            loadFeed();
        });
    }

    private void loadFeed() {
        repository.getPostsByCategory(selectedCategory, posts -> adapter.submit(posts));
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

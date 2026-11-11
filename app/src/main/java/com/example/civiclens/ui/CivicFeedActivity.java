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
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) { }
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
        toolbar.setNavigationOnClickListener(v -> IntentHelper.handleBackNavigation(this));
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                IntentHelper.handleBackNavigation(CivicFeedActivity.this);
            }
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
            Toast.makeText(this, "Please write your civic concern or update first.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        String author = auth.currentEmail() != null ? auth.currentEmail() : "Citizen Activist";
        String postCategory = "ALL".equalsIgnoreCase(selectedCategory) ? "Environment" : selectedCategory;

        com.example.civiclens.model.CivicPost newPost = new com.example.civiclens.model.CivicPost();
        newPost.id = java.util.UUID.randomUUID().toString();
        newPost.authorName = author;
        newPost.category = postCategory;
        newPost.district = "Dhaka";
        newPost.postType = "text";
        newPost.content = content;
        newPost.mediaUrl = attachedMediaUri;
        newPost.createdAt = System.currentTimeMillis();

        adapter.addPostAtTop(newPost);
        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "🌱 Civic Post Published Live to Feed!", false);
        postInput.setText("");
        attachedMediaUri = "";

        repository.createPost(author, postCategory, "Dhaka", "text", content, newPost.mediaUrl, () -> loadFeed());
    }

    private com.google.firebase.firestore.ListenerRegistration firestoreFeedListener;

    private void loadFeed() {
        if (com.example.civiclens.firebase.FirebaseGate.isReady(this)) {
            try {
                if (firestoreFeedListener != null) {
                    firestoreFeedListener.remove();
                    firestoreFeedListener = null;
                }

                firestoreFeedListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("posts")
                        .addSnapshotListener((snapshot, error) -> {
                            if (error != null || snapshot == null || snapshot.isEmpty()) {
                                repository.getPostsByCategory(selectedCategory, posts -> {
                                    if (!isFinishing() && !isDestroyed()) {
                                        adapter.submit(posts != null ? posts : new java.util.ArrayList<>());
                                    }
                                });
                                return;
                            }

                            java.util.List<com.example.civiclens.model.CivicPost> liveList = new java.util.ArrayList<>();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                String content = doc.getString("content");
                                String cat = doc.getString("category");
                                Long createdAt = doc.contains("createdAt") && doc.getLong("createdAt") != null ? doc.getLong("createdAt") : System.currentTimeMillis();

                                if (content != null && !content.trim().isEmpty()) {
                                    if ("ALL".equalsIgnoreCase(selectedCategory) || selectedCategory == null || selectedCategory.isEmpty()
                                            || (cat != null && cat.equalsIgnoreCase(selectedCategory))) {
                                        com.example.civiclens.model.CivicPost p = new com.example.civiclens.model.CivicPost();
                                        p.id = doc.getId();
                                        p.authorName = doc.getString("authorName") != null ? doc.getString("authorName") : "Citizen Activist";
                                        p.category = cat != null ? cat : "Environment";
                                        p.district = doc.getString("district") != null ? doc.getString("district") : "Dhaka";
                                        p.content = content;
                                        p.mediaUrl = doc.getString("mediaUrl") != null ? doc.getString("mediaUrl") : "";
                                        p.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();

                                        Long supportCount = doc.getLong("supportCount");
                                        Long commentCount = doc.getLong("commentCount");
                                        p.supportCount = supportCount != null ? supportCount.intValue() : 0;
                                        p.commentCount = commentCount != null ? commentCount.intValue() : 0;

                                        liveList.add(p);
                                    }
                                }
                            }

                            java.util.Collections.sort(liveList, (p1, p2) -> Long.compare(p2.createdAt, p1.createdAt));

                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    adapter.submit(liveList);
                                }
                            });
                        });
                return;
            } catch (Exception ignored) { }
        }

        loadLocalFeed();
    }

    private void loadLocalFeed() {
        repository.getPostsByCategory(selectedCategory, posts -> {
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    adapter.submit(posts != null ? posts : new java.util.ArrayList<>());
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        repository.startPostSync(() -> loadFeed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFeed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreFeedListener != null) {
            firestoreFeedListener.remove();
            firestoreFeedListener = null;
        }
        repository.stopPostSync();
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

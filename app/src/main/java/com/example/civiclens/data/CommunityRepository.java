package com.example.civiclens.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.firebase.FirebaseGate;
import com.example.civiclens.model.CivicCommunity;
import com.example.civiclens.model.CivicPost;
import com.example.civiclens.model.GroupMessage;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommunityRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    public interface ResultCallback {
        void onSuccess();
        void onError(String error);
    }

    private static final String DB_URL =
            "https://civiclens-e2000-default-rtdb.asia-southeast1.firebasedatabase.app";

    private final CommunityDao dao;
    private final DatabaseReference root;
    private final Handler main = new Handler(Looper.getMainLooper());

    private ValueEventListener communityListener;
    private ValueEventListener postListener;

    public CommunityRepository(Context context) {
        dao = CivicDatabase.get(context).communityDao();
        if (FirebaseGate.isReady(context)) {
            root = FirebaseDatabase.getInstance(DB_URL).getReference();
        } else {
            root = null;
        }
    }

    private String getUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void showError(ResultCallback callback, String error) {
        main.post(() -> {
            if (callback != null) callback.onError(error);
        });
    }

    private <T> void deliver(Callback<T> callback, T value) {
        main.post(() -> {
            if (callback != null) callback.onResult(value);
        });
    }

    public void seedIfEmpty(Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            List<CivicCommunity> existing = dao.getAllCommunities();
            if (existing.isEmpty()) {
                List<CivicCommunity> demoGroups = new ArrayList<>();

                CivicCommunity c1 = new CivicCommunity();
                c1.id = "comm_1";
                c1.name = "Save the Sundarbans & Coastal Forests";
                c1.description = "Citizen environmental protection alliance safeguarding mangrove forests, coastal embankments, & wildlife.";
                c1.category = "Environment";
                c1.district = "Khulna";
                c1.communityType = CivicCommunity.TYPE_PUBLIC;
                c1.memberCount = 128;
                demoGroups.add(c1);

                CivicCommunity c2 = new CivicCommunity();
                c2.id = "comm_2";
                c2.name = "Protect Khulna Wetlands & Canal Alliance";
                c2.description = "Community monitoring against illegal wetland encroachment, canal blocking, & industrial pollution.";
                c2.category = "Environment";
                c2.district = "Khulna";
                c2.communityType = CivicCommunity.TYPE_PUBLIC;
                c2.memberCount = 84;
                demoGroups.add(c2);

                CivicCommunity c3 = new CivicCommunity();
                c3.id = "comm_3";
                c3.name = "Safe Roads & Infrastructure Assembly — Gazipur / Dhaka";
                c3.description = "Advocacy for pedestrian flyovers, road repair speed, and BRT corridor safety inspection.";
                c3.category = "Infrastructure";
                c3.district = "Gazipur";
                c3.communityType = CivicCommunity.TYPE_PUBLIC;
                c3.memberCount = 210;
                demoGroups.add(c3);

                CivicCommunity c4 = new CivicCommunity();
                c4.id = "comm_4";
                c4.name = "Stop Illegal River Encroachment — Buriganga & Turag";
                c4.description = "River conservation assembly monitoring riverbank boundaries, waste dumping, & water quality.";
                c4.category = "Environment";
                c4.district = "Dhaka";
                c4.communityType = CivicCommunity.TYPE_PUBLIC;
                c4.memberCount = 156;
                demoGroups.add(c4);

                CivicCommunity c5 = new CivicCommunity();
                c5.id = "comm_5";
                c5.name = "Public Transport & Metro Rail Improvement Forum";
                c5.description = "Citizen forum advocating for expanded bus routes, ticket transparency, and accessibility for disabled passengers.";
                c5.category = "Infrastructure";
                c5.district = "Dhaka";
                c5.communityType = CivicCommunity.TYPE_PUBLIC;
                c5.memberCount = 340;
                demoGroups.add(c5);

                CivicCommunity c6 = new CivicCommunity();
                c6.id = "comm_6";
                c6.name = "Protect Local Forests & Biodiversity — Sylhet";
                c6.description = "Eco-protection group safeguarding tea garden ecosystems, hill cutting prevention, and reforestation.";
                c6.category = "Environment";
                c6.district = "Sylhet";
                c6.communityType = CivicCommunity.TYPE_PUBLIC;
                c6.memberCount = 92;
                demoGroups.add(c6);

                dao.insertCommunities(demoGroups);
            }

            List<CivicPost> existingPosts = dao.getAllPosts();
            if (existingPosts.isEmpty()) {
                List<CivicPost> demoPosts = new ArrayList<>();

                CivicPost p1 = new CivicPost();
                p1.id = "post_1";
                p1.authorName = "Save the Wetlands Community";
                p1.category = "Environment";
                p1.district = "Khulna";
                p1.postType = CivicPost.TYPE_TEXT;
                p1.content = "Concern regarding the proposed development project near the wetland. Community members are sharing observations about potential environmental impact. Join the discussion and review project documents.";
                p1.supportCount = 128;
                p1.commentCount = 24;
                p1.shareCount = 18;
                demoPosts.add(p1);

                CivicPost p2 = new CivicPost();
                p2.id = "post_2";
                p2.authorName = "Gazipur Road Safety Forum";
                p2.category = "Infrastructure";
                p2.district = "Gazipur";
                p2.postType = CivicPost.TYPE_PHOTO;
                p2.content = "Ground observation of Tongi-Gazipur BRT corridor drainage work. Construction progress is delayed by 4 months causing severe traffic backlog.";
                p2.supportCount = 94;
                p2.commentCount = 15;
                p2.shareCount = 12;
                demoPosts.add(p2);

                dao.insertPosts(demoPosts);
            }

            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void createCommunity(String name, String desc, String category, String district, String type, Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            CivicCommunity c = new CivicCommunity();
            c.id = UUID.randomUUID().toString();
            c.name = name;
            c.description = desc;
            c.category = category;
            c.district = district;
            c.communityType = type;
            c.memberCount = 1;
            c.isMember = true;
            c.createdAt = System.currentTimeMillis();

            dao.insertCommunity(c);

            if (root != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", c.id);
                data.put("name", name.trim());
                data.put("description", desc);
                data.put("category", category);
                data.put("district", district);
                data.put("communityType", type);
                data.put("createdBy", getUserId() != null ? getUserId() : "local");
                data.put("createdAt", System.currentTimeMillis());
                data.put("memberCount", 1);

                root.child("communities").child(c.id).setValue(data);
            }

            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void joinCommunityByName(String name, Runnable onDone) {
        if (name == null || name.isEmpty()) return;
        CivicDatabase.IO.execute(() -> {
            List<CivicCommunity> all = dao.getAllCommunities();
            boolean found = false;
            for (CivicCommunity c : all) {
                if (c.name.equalsIgnoreCase(name.trim())) {
                    if (!c.isMember) {
                        c.isMember = true;
                        c.memberCount++;
                        dao.insertCommunity(c);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                CivicCommunity c = new CivicCommunity();
                c.id = UUID.randomUUID().toString();
                c.name = name.trim();
                c.description = "Citizen environmental and public advocacy group.";
                c.category = "Environment";
                c.district = "Dhaka";
                c.communityType = CivicCommunity.TYPE_PUBLIC;
                c.memberCount = 2;
                c.isMember = true;
                dao.insertCommunity(c);
            }
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void joinCommunity(String communityId, ResultCallback callback) {
        String uid = getUserId();

        if (communityId == null || communityId.trim().isEmpty()) {
            showError(callback, "Invalid community ID.");
            return;
        }

        CivicDatabase.IO.execute(() -> {
            CivicCommunity c = dao.getCommunityById(communityId);
            if (c != null && !c.isMember) {
                c.isMember = true;
                c.memberCount++;
                dao.insertCommunity(c);
            }
            if (callback != null) {
                main.post(callback::onSuccess);
            }
        });

        if (root != null && uid != null) {
            DatabaseReference communityRef = root.child("communities").child(communityId);
            communityRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData data) {
                    if (data.getValue() == null) {
                        return Transaction.abort();
                    }
                    MutableData members = data.child("members");
                    if (Boolean.TRUE.equals(members.child(uid).getValue(Boolean.class))) {
                        return Transaction.abort();
                    }
                    members.child(uid).setValue(true);
                    Integer count = data.child("memberCount").getValue(Integer.class);
                    data.child("memberCount").setValue(count == null ? 1 : count + 1);
                    return Transaction.success(data);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) { }
            });
        }
    }

    public void createPost(String authorName, String category, String district, String type, String content, String mediaUrl, Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            CivicPost p = new CivicPost();
            p.id = UUID.randomUUID().toString();
            p.authorName = authorName == null || authorName.isEmpty() ? "Citizen Activist" : authorName;
            p.category = category == null || category.isEmpty() ? "Environment" : category;
            p.district = district == null || district.isEmpty() ? "Dhaka" : district;
            p.postType = type;
            p.content = content;
            p.mediaUrl = mediaUrl;
            p.createdAt = System.currentTimeMillis();

            dao.insertPost(p);

            if (root != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", p.id);
                data.put("authorName", p.authorName);
                data.put("category", category);
                data.put("district", district);
                data.put("postType", type);
                data.put("content", content);
                data.put("mediaUrl", mediaUrl);
                data.put("createdAt", p.createdAt);

                root.child("posts").child(p.id).setValue(data);
            }

            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void registerCloudMembership(String groupId, String groupName, Runnable onDone) {
        String uid = getUserId();
        CivicDatabase.IO.execute(() -> {
            CivicCommunity c = dao.getCommunityById(groupId);
            if (c == null) {
                c = new CivicCommunity();
                c.id = groupId;
                c.name = groupName;
                c.description = "Citizen environmental and public advocacy group.";
                c.category = "Environment";
                c.district = "Dhaka";
                c.communityType = CivicCommunity.TYPE_PUBLIC;
                c.memberCount = 2;
                c.isMember = true;
            } else {
                c.isMember = true;
                c.memberCount++;
            }
            dao.insertCommunity(c);

            if (root != null && uid != null) {
                Map<String, Object> uData = new HashMap<>();
                uData.put("groupId", groupId);
                uData.put("groupName", groupName);
                uData.put("joinedAt", System.currentTimeMillis());
                root.child("users").child(uid).child("joined_groups").child(groupId).setValue(uData);

                Map<String, Object> mData = new HashMap<>();
                mData.put("uid", uid);
                mData.put("email", FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "citizen");
                mData.put("joinedAt", System.currentTimeMillis());
                root.child("groups").child(groupId).child("members").child(uid).setValue(mData);
            }

            if (onDone != null) main.post(onDone);
        });
    }

    public void startCommunitySync(String uid) {
        if (root == null || uid == null) return;
        stopCommunitySync();
        communityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                CivicDatabase.IO.execute(() -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        CivicCommunity c = child.getValue(CivicCommunity.class);
                        if (c == null) continue;
                        c.id = child.getKey();
                        c.isMember = child.child("members").hasChild(uid);
                        dao.insertCommunity(c);
                    }
                });
            }
            @Override public void onCancelled(DatabaseError error) { }
        };
        root.child("communities").addValueEventListener(communityListener);

        root.child("users").child(uid).child("joined_groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                CivicDatabase.IO.execute(() -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String gId = child.child("groupId").getValue(String.class);
                        String gName = child.child("groupName").getValue(String.class);
                        if (gId != null && gName != null) {
                            CivicCommunity c = dao.getCommunityById(gId);
                            if (c == null) {
                                c = new CivicCommunity();
                                c.id = gId;
                                c.name = gName;
                                c.description = "Joined Civic Assembly Group";
                                c.category = "Environment";
                                c.district = "Dhaka";
                                c.memberCount = 2;
                            }
                            c.isMember = true;
                            dao.insertCommunity(c);
                        }
                    }
                });
            }
            @Override public void onCancelled(DatabaseError error) { }
        });
    }

    public void startPostSync() {
        if (root == null) return;
        stopPostSync();
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                CivicDatabase.IO.execute(() -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        CivicPost p = child.getValue(CivicPost.class);
                        if (p == null) continue;
                        p.id = child.getKey();
                        dao.insertPost(p);
                    }
                });
            }
            @Override public void onCancelled(DatabaseError error) { }
        };
        root.child("posts").addValueEventListener(postListener);
    }

    public void stopCommunitySync() {
        if (root != null && communityListener != null) {
            root.child("communities").removeEventListener(communityListener);
            communityListener = null;
        }
    }

    public void stopPostSync() {
        if (root != null && postListener != null) {
            root.child("posts").removeEventListener(postListener);
            postListener = null;
        }
    }

    public void stopAllSync() {
        stopCommunitySync();
        stopPostSync();
    }

    public void getAllCommunities(Callback<List<CivicCommunity>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getAllCommunities()));
    }

    public void getAllPosts(Callback<List<CivicPost>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getAllPosts()));
    }

    public void getPostsByCategory(String category, Callback<List<CivicPost>> callback) {
        CivicDatabase.IO.execute(() -> {
            if ("ALL".equalsIgnoreCase(category) || category == null || category.trim().isEmpty()) {
                deliver(callback, dao.getAllPosts());
            } else {
                List<CivicPost> filtered = dao.getPostsByCategory(category.trim());
                if (filtered == null || filtered.isEmpty()) {
                    List<CivicPost> all = dao.getAllPosts();
                    List<CivicPost> matches = new ArrayList<>();
                    for (CivicPost p : all) {
                        if (p.category != null && (p.category.equalsIgnoreCase(category.trim()) || p.category.contains(category.trim()))) {
                            matches.add(p);
                        }
                    }
                    deliver(callback, matches.isEmpty() ? all : matches);
                } else {
                    deliver(callback, filtered);
                }
            }
        });
    }

    public void supportPost(String postId, Runnable onDone) {
        if (postId == null || postId.isEmpty()) return;
        CivicDatabase.IO.execute(() -> {
            List<CivicPost> all = dao.getAllPosts();
            for (CivicPost p : all) {
                if (p.id.equals(postId)) {
                    p.supportCount++;
                    dao.insertPost(p);
                    if (root != null) {
                        root.child("posts").child(postId).child("supportCount").setValue(p.supportCount);
                    }
                    break;
                }
            }
            if (onDone != null) main.post(onDone);
        });
    }

    public void addCommentToPost(String postId, String commentText, Runnable onDone) {
        if (postId == null || commentText == null || commentText.trim().isEmpty()) return;
        CivicDatabase.IO.execute(() -> {
            List<CivicPost> all = dao.getAllPosts();
            for (CivicPost p : all) {
                if (p.id.equals(postId)) {
                    p.commentCount++;
                    dao.insertPost(p);
                    if (root != null) {
                        root.child("posts").child(postId).child("commentCount").setValue(p.commentCount);
                        String commentId = UUID.randomUUID().toString();
                        Map<String, Object> cData = new HashMap<>();
                        cData.put("id", commentId);
                        cData.put("text", commentText);
                        cData.put("timestamp", System.currentTimeMillis());
                        root.child("posts").child(postId).child("comments").child(commentId).setValue(cData);
                    }
                    break;
                }
            }
            if (onDone != null) main.post(onDone);
        });
    }

    public void sendGroupMessage(String groupId, String senderName, String text, Runnable onDone) {
        if (groupId == null || text == null || text.trim().isEmpty()) return;
        CivicDatabase.IO.execute(() -> {
            GroupMessage m = new GroupMessage();
            m.id = UUID.randomUUID().toString();
            m.groupId = groupId;
            m.senderName = senderName != null ? senderName : "Citizen Activist";
            m.text = text.trim();
            m.createdAt = System.currentTimeMillis();

            dao.insertMessage(m);

            if (root != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", m.id);
                data.put("groupId", groupId);
                data.put("senderName", m.senderName);
                data.put("text", m.text);
                data.put("createdAt", m.createdAt);
                root.child("groups").child(groupId).child("messages").child(m.id).setValue(data);
            }

            if (onDone != null) main.post(onDone);
        });
    }

    public ValueEventListener listenForGroupMessages(String groupId, Callback<List<GroupMessage>> callback) {
        if (root == null || groupId == null) return null;
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<GroupMessage> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String sender = child.child("senderName").getValue(String.class);
                    String text = child.child("text").getValue(String.class);
                    if (text != null) {
                        GroupMessage m = new GroupMessage();
                        m.id = child.getKey();
                        m.groupId = groupId;
                        m.senderName = sender != null ? sender : "Citizen Activist";
                        m.text = text;
                        list.add(m);
                    }
                }
                deliver(callback, list);
            }

            @Override public void onCancelled(DatabaseError error) { }
        };
        root.child("groups").child(groupId).child("messages").addValueEventListener(listener);
        return listener;
    }
}

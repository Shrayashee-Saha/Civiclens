package com.example.civiclens.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.model.CivicCommunity;
import com.example.civiclens.model.CivicPost;
import com.example.civiclens.model.GroupMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommunityRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    private final CommunityDao dao;
    private final Handler main = new Handler(Looper.getMainLooper());

    public CommunityRepository(Context context) {
        this.dao = CivicDatabase.get(context).communityDao();
    }

    private <T> void deliver(Callback<T> callback, T value) {
        main.post(() -> callback.onResult(value));
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

    public void getAllPosts(Callback<List<CivicPost>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getAllPosts()));
    }

    public void getPostsByCategory(String category, Callback<List<CivicPost>> callback) {
        CivicDatabase.IO.execute(() -> {
            if ("ALL".equalsIgnoreCase(category) || category == null || category.isEmpty()) {
                deliver(callback, dao.getAllPosts());
            } else {
                deliver(callback, dao.getPostsByCategory(category));
            }
        });
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
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void getAllCommunities(Callback<List<CivicCommunity>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getAllCommunities()));
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
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }
}

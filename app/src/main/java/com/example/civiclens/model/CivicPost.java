package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a post in the Civic Feed.
 * Supports 5 post types: text, photo, video, news, project_update.
 */
@Entity(tableName = "civic_posts")
public class CivicPost {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_NEWS = "news";
    public static final String TYPE_PROJECT_UPDATE = "project_update";

    @PrimaryKey
    @NonNull
    public String id = "";

    public String authorName = "";
    public String authorEmail = "";
    public String category = ""; // Environment, Infrastructure, Community
    public String district = "";
    public String postType = TYPE_TEXT;
    public String content = "";
    public String mediaUrl = ""; // Photo or Video URL
    public String linkedProjectId = "";
    public String linkedProjectName = "";
    public String linkedCommunityId = "";
    public String linkedCommunityName = "";
    public int supportCount = 0;
    public int commentCount = 0;
    public int shareCount = 0;
    public long createdAt = System.currentTimeMillis();
}

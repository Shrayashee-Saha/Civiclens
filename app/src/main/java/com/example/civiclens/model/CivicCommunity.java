package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a Civic Group / Community in Bangladesh.
 */
@Entity(tableName = "civic_communities")
public class CivicCommunity {

    public static final String TYPE_PUBLIC = "Public Community";
    public static final String TYPE_PRIVATE = "Private Community";
    public static final String TYPE_PROJECT_SPECIFIC = "Project-Specific Community";

    @PrimaryKey
    @NonNull
    public String id = "";

    public String name = "";
    public String description = "";
    public String category = ""; // Environment, Infrastructure, Safe Roads, River Protection
    public String district = "";
    public String communityType = TYPE_PUBLIC; // Public, Private, Project-Specific
    public String linkedProjectId = "";
    public String coverImageUrl = "";
    public String rulesNote = "";
    public int memberCount = 1;
    public boolean isMember = false;
    public long createdAt = System.currentTimeMillis();
    public String createdBy = "";

    @androidx.room.Ignore
    public java.util.Map<String, Boolean> members = new java.util.HashMap<>();

    public CivicCommunity() { }
}

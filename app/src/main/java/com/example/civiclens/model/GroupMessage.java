package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a chat message in a Civic Group.
 */
@Entity(tableName = "group_messages")
public class GroupMessage {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String groupId = "";
    public String senderName = "";
    public String senderEmail = "";
    public String text = "";
    public String mediaUrl = ""; // Photo, Video, or Document Attachment URL
    public long createdAt = System.currentTimeMillis();
}

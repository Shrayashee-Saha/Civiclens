package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Audit trail entry. Events are only ever added, never edited or deleted. */
@Entity(tableName = "project_events", indices = {@Index("projectId")})
public class ProjectEvent {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String projectId = "";

    public String description = "";
    public long createdAt;

    public ProjectEvent() { }
}

package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A planned milestone. completedDate is null until a completion is recorded. Dates are yyyy-MM-dd. */
@Entity(tableName = "project_milestones", indices = {@Index("projectId")})
public class ProjectMilestone {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String projectId = "";

    public String title = "";
    public String plannedDate = "";
    public String completedDate;      // null when no completion is recorded
    public int sortOrder;

    public ProjectMilestone() { }
}

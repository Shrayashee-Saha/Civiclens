package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Citizen project contribution submitted using news URLs or official procurement records.
 */
@Entity(tableName = "project_contributions")
public class ProjectContribution {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String projectName = "";
    public String newsUrl = "";
    public String district = "";
    public String upazila = "";
    public String additionalInfo = "";
    public String status = "Pending Source Verification";
    public long createdAt;

    public ProjectContribution() { }
}

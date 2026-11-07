package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A citizen observation submitted for a public infrastructure project.
 * Evidence review workflow tracks report status from submission to resolution.
 */
@Entity(tableName = "project_observations", indices = {@Index("projectId")})
public class ProjectObservation {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_VERIFIED = "VERIFIED";
    public static final String STATUS_FORWARDED = "FORWARDED";
    public static final String STATUS_RESPONSE_RECEIVED = "RESPONSE_RECEIVED";
    public static final String STATUS_RESOLVED = "RESOLVED";

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String projectId = "";

    public String text = "";
    public String photoUri;
    public String locationNote = "";
    public String status = STATUS_UNDER_REVIEW;
    public long createdAt;

    public ProjectObservation() { }
}

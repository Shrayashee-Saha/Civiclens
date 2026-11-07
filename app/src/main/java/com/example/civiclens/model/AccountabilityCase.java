package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Public Interest Accountability Case / Investigation record linked to news & official reports.
 */
@Entity(tableName = "accountability_cases", indices = {@Index("projectId")})
public class AccountabilityCase {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String projectId = ""; // Optional link to a PublicProject ID
    public String title = "";
    public String publisher = ""; // e.g. "The Business Standard", "UNB", "Dhaka Tribune"
    public String newsUrl = "";
    public String imageUrl = "";  // Geotagged news article photo URL
    public String reportedConcern = "";
    public String authority = ""; // e.g. "Anti-Corruption Commission (ACC)", "IMED"
    public String publicationDate = ""; // "2025-04-29"
    public String investigationStatus = "Reported"; // "Reported", "Under Review", "ACC Inquiry", "Case Filed", "Finding Established"
    public String sourceStatus = "";
    public String locationName = "";
    public double latitude = 0.0;
    public double longitude = 0.0;
    public long financialImpactBdt = 0;

    public AccountabilityCase() { }
}

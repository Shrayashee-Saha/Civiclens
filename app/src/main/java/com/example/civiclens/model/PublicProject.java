package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A public infrastructure project in Bangladesh.
 */
@Entity(tableName = "projects", indices = {@Index("district")})
public class PublicProject {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String name = "";
    public String agency = "";
    public String contractor = "";
    public String district = "";
    public String upazila = "";
    public String category = "";
    public String sourceNote = "";
    public String lastUpdated = "";        // "yyyy-MM-dd"
    public String startDate = "";          // "yyyy-MM-dd"
    public String expectedCompletion = ""; // "yyyy-MM-dd"
    public String status = "Ongoing";      // "Ongoing", "Delayed", "Completed", "Suspended"
    public int currentProgressPercent = 0;
    public int plannedProgressPercent = 0;
    public long budgetBdt;
    public boolean fictional = false;

    public PublicProject() { }
}

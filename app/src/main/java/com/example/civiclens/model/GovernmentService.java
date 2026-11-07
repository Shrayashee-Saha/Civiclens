package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "services")
public class GovernmentService {

    @PrimaryKey
    @NonNull
    public String id = "";

    public String nameEn = "";
    public String nameBn = "";
    public String category = "";
    public String authority = "";
    public String description = "";
    /** Comma-separated search keywords, for example "birth,certificate,copy". */
    public String keywords = "";

    public String portalUrl;      // official portal, null if not known
    public String sourceUrl;      // page where the information was published
    public String lastVerified;   // "YYYY-MM-DD", or "NOT VERIFIED"
    public String feesNote = "Not verified";
    public String processingNote = "Not verified";

    public boolean onlineApplication;
    public boolean physicalVisitMayBeRequired;
    /** True for every record in this milestone. Demo records are labelled in the UI. */
    public boolean demoData = true;

    public GovernmentService() { }
}

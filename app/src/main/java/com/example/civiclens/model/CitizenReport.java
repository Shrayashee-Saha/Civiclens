package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A problem the citizen reported.
 *
 * status says what the citizen did on the device (draft, prepared, possible duplicate).
 * syncState says whether the text reached the CivicLens server (LOCAL, QUEUED, RECEIVED).
 * serverStatus is what the server reports (RECEIVED, FORWARDED, OFFICE_CONFIRMED, RESOLVED).
 * Only the server can set OFFICE_CONFIRMED or RESOLVED. The app never sets them.
 */
@Entity(tableName = "reports", indices = {@Index("district"), @Index("category"), @Index("syncState")})
public class CitizenReport {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PREPARED = "PREPARED_TO_SEND";
    public static final String STATUS_DUPLICATE = "POSSIBLE_DUPLICATE";

    public static final String SYNC_LOCAL = "LOCAL";
    public static final String SYNC_QUEUED = "QUEUED";
    public static final String SYNC_RECEIVED = "RECEIVED";

    @PrimaryKey
    @NonNull
    public String id = "";

    public String category = "";
    public String description = "";
    public String district = "";
    public String locationNote = "";
    public String photoUri;           // stays on this device; photos are not uploaded yet
    public String officeId;
    public String officeName;
    public String status = STATUS_DRAFT;
    public String syncState = SYNC_LOCAL;
    public String serverId;           // set once CivicLens has received the report
    public String serverStatus;       // RECEIVED, FORWARDED, OFFICE_CONFIRMED, or RESOLVED
    public long createdAt;

    public CitizenReport() { }
}

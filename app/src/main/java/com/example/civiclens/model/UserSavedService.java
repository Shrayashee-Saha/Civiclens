package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A bookmark the user created, for either a service or an office. */
@Entity(tableName = "saved_items")
public class UserSavedService {

    public static final String KIND_SERVICE = "service";
    public static final String KIND_OFFICE = "office";

    @PrimaryKey
    @NonNull
    public String savedKey = "";      // "service:birth_copy" or "office:off_birth_1"

    @NonNull
    public String kind = "";
    @NonNull
    public String targetId = "";
    public long savedAt;

    public UserSavedService() { }

    public static String keyOf(String kind, String targetId) {
        return kind + ":" + targetId;
    }
}

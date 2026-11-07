package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** The user's own progress. Stores only a checkbox state, never the document itself. */
@Entity(tableName = "user_checklist")
public class UserChecklist {

    @PrimaryKey
    @NonNull
    public String documentId = "";

    public boolean collected;
    public long updatedAt;

    public UserChecklist() { }
}

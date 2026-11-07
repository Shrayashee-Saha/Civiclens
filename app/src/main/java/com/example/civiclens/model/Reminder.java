package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A user-set reminder for a service deadline. dueDate uses yyyy-MM-dd. */
@Entity(tableName = "reminders", indices = {@Index("serviceId")})
public class Reminder {

    @PrimaryKey
    @NonNull
    public String id = "";            // serviceId + "_" + dueDate

    @NonNull
    public String serviceId = "";
    public String title = "";
    public String dueDate = "";
    public boolean notified;
    public boolean done;

    public Reminder() { }
}

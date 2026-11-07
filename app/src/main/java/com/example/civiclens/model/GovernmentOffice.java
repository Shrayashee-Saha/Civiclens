package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * An office or help point. TYPE_AUTHORIZED means the office processes the service itself.
 * TYPE_ASSISTANCE means the centre only helps with forms and does NOT process applications.
 * phone and email stay null until verified. The app shows Call, Message, and Email only when set.
 */
@Entity(tableName = "offices", indices = {@Index("district")})
public class GovernmentOffice {

    public static final String TYPE_AUTHORIZED = "AUTHORIZED_OFFICE";
    public static final String TYPE_ASSISTANCE = "ASSISTANCE_CENTER";
    public static final String TYPE_GENERAL = "GENERAL_OFFICE";

    @PrimaryKey
    @NonNull
    public String id = "";

    public String nameEn = "";
    public String nameBn = "";
    public String type = TYPE_AUTHORIZED;
    public String district = "";
    public String upazila = "";
    public String unionName = "";     // not "union": a reserved word in SQLite
    public String servesKeywords = "";
    public String address = "Address not verified";
    public String phone;              // null until verified
    public String email;              // null until verified
    public String openingHours = "Not verified";
    public String lastVerified = "NOT VERIFIED";
    public boolean demoData = true;

    public GovernmentOffice() { }
}

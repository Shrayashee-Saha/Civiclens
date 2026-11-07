package com.example.civiclens.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "service_documents", indices = {@Index("serviceId")})
public class ServiceDocument {

    @PrimaryKey
    @NonNull
    public String id = "";          // unique key, for example "birth_copy_1"

    @NonNull
    public String serviceId = "";

    public String title = "";
    public String note = "";
    public boolean mandatory = true; // false means conditional
    public int sortOrder;

    public ServiceDocument() { }
}

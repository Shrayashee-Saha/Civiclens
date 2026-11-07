package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.UserChecklist;

import java.util.List;

@Dao
public interface ChecklistDao {

    @Query("SELECT c.* FROM user_checklist c " +
           "INNER JOIN service_documents d ON c.documentId = d.id " +
           "WHERE d.serviceId = :serviceId")
    List<UserChecklist> getForService(String serviceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserChecklist item);
}

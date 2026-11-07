package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.ServiceDocument;

import java.util.List;

@Dao
public interface ServiceDao {

    @Query("SELECT * FROM services ORDER BY nameEn")
    List<GovernmentService> getAll();

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    GovernmentService getById(String id);

    @Query("SELECT * FROM service_documents WHERE serviceId = :serviceId " +
           "ORDER BY mandatory DESC, sortOrder ASC")
    List<ServiceDocument> getDocuments(String serviceId);

    @Query("SELECT COUNT(*) FROM services")
    int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertServices(List<GovernmentService> services);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDocuments(List<ServiceDocument> documents);
}

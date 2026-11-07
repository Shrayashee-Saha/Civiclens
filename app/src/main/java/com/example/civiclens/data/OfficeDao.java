package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.GovernmentOffice;

import java.util.List;

@Dao
public interface OfficeDao {

    @Query("SELECT * FROM offices ORDER BY district, nameEn")
    List<GovernmentOffice> getAll();

    @Query("SELECT * FROM offices WHERE id = :id LIMIT 1")
    GovernmentOffice getById(String id);

    @Query("SELECT COUNT(*) FROM offices")
    int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GovernmentOffice> offices);
}

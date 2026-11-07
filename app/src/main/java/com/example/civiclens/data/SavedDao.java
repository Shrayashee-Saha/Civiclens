package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.UserSavedService;

import java.util.List;

@Dao
public interface SavedDao {

    @Query("SELECT * FROM saved_items WHERE kind = :kind ORDER BY savedAt DESC")
    List<UserSavedService> getByKind(String kind);

    @Query("SELECT COUNT(*) FROM saved_items WHERE savedKey = :key")
    int exists(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(UserSavedService item);

    @Query("DELETE FROM saved_items WHERE savedKey = :key")
    void remove(String key);
}

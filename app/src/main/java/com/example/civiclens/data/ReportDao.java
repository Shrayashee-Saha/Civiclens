package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.CitizenReport;

import java.util.List;

@Dao
public interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    List<CitizenReport> getAll();

    @Query("SELECT * FROM reports WHERE category = :category AND district = :district")
    List<CitizenReport> getScoped(String category, String district);

    @Query("SELECT * FROM reports WHERE syncState = 'QUEUED'")
    List<CitizenReport> getQueued();

    /** Reports the server has received but whose status may still change. */
    @Query("SELECT * FROM reports WHERE syncState = 'RECEIVED' AND serverStatus != 'RESOLVED'")
    List<CitizenReport> getTracked();

    @Query("UPDATE reports SET syncState = :syncState, serverId = :serverId, serverStatus = :serverStatus "
            + "WHERE id = :id")
    void updateSync(String id, String syncState, String serverId, String serverStatus);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CitizenReport report);
}

package com.example.civiclens.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.civiclens.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE serviceId = :serviceId AND done = 0 ORDER BY dueDate")
    List<Reminder> getActiveForService(String serviceId);

    @Query("SELECT * FROM reminders WHERE done = 0 AND notified = 0 AND dueDate <= :horizon")
    List<Reminder> getDueUntil(String horizon);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Reminder reminder);

    @Query("UPDATE reminders SET notified = 1 WHERE id = :id")
    void markNotified(String id);

    @Query("UPDATE reminders SET done = 1 WHERE id = :id")
    void markDone(String id);
}

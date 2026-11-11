package com.example.civiclens.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.civiclens.model.CitizenReport;
import com.example.civiclens.model.GovernmentOffice;
import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.ProjectEvent;
import com.example.civiclens.model.ProjectMilestone;
import com.example.civiclens.model.ProjectObservation;
import com.example.civiclens.model.PublicProject;
import com.example.civiclens.model.Reminder;
import com.example.civiclens.model.ServiceDocument;
import com.example.civiclens.model.UserChecklist;
import com.example.civiclens.model.UserSavedService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                GovernmentService.class,
                ServiceDocument.class,
                UserChecklist.class,
                GovernmentOffice.class,
                UserSavedService.class,
                PublicProject.class,
                ProjectMilestone.class,
                ProjectObservation.class,
                ProjectEvent.class,
                Reminder.class,
                CitizenReport.class,
                com.example.civiclens.model.AccountabilityCase.class,
                com.example.civiclens.model.ProjectContribution.class,
                com.example.civiclens.model.CivicPost.class,
                com.example.civiclens.model.CivicCommunity.class,
                com.example.civiclens.model.GroupMessage.class
        },
        version = 11,
        exportSchema = false)
public abstract class CivicDatabase extends RoomDatabase {

    /** One worker for all database work, so writes finish before the reads queued after them. */
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private static volatile CivicDatabase instance;

    public abstract ServiceDao serviceDao();
    public abstract ChecklistDao checklistDao();
    public abstract OfficeDao officeDao();
    public abstract SavedDao savedDao();
    public abstract ProjectDao projectDao();
    public abstract ReminderDao reminderDao();
    public abstract ReportDao reportDao();
    public abstract CommunityDao communityDao();

    public static CivicDatabase get(Context context) {
        if (instance == null) {
            synchronized (CivicDatabase.class) {
                if (instance == null) {
                    // Schema versions 1 to 6 were never released, so rebuilding the local
                    // cache is acceptable here. A public release needs a real Migration.
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CivicDatabase.class,
                            "civiclens.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}

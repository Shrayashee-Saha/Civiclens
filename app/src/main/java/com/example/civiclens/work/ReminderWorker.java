package com.example.civiclens.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.civiclens.data.CivicDatabase;
import com.example.civiclens.data.ReminderDao;
import com.example.civiclens.domain.DateMath;
import com.example.civiclens.domain.ProjectAnalyzer;
import com.example.civiclens.model.Reminder;
import com.example.civiclens.notify.NotificationHelper;

import java.util.List;

/**
 * Background check, run once a day by WorkManager. It notifies for reminders due within the
 * next few days, including overdue ones. Each reminder notifies once.
 */
public class ReminderWorker extends Worker {

    public static final String UNIQUE_NAME = "civiclens_reminder_check";
    public static final int LOOKAHEAD_DAYS = 3;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context app = getApplicationContext();
        ReminderDao dao = CivicDatabase.get(app).reminderDao();
        String horizon = DateMath.addDays(ProjectAnalyzer.todayIso(), LOOKAHEAD_DAYS);

        List<Reminder> due = dao.getDueUntil(horizon);
        for (Reminder reminder : due) {
            // Only mark as notified if the notification was actually shown, so it can retry later.
            if (NotificationHelper.showReminder(app, reminder)) {
                dao.markNotified(reminder.id);
            }
        }
        return Result.success();
    }
}

package com.example.civiclens;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.civiclens.notify.NotificationHelper;
import com.example.civiclens.sync.SyncScheduler;
import com.example.civiclens.work.ReminderWorker;

import java.util.concurrent.TimeUnit;

/** Runs once per process: notification channels, the daily reminder check, and report sync. */
public class CivicLensApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);

        PeriodicWorkRequest dailyCheck =
                new PeriodicWorkRequest.Builder(ReminderWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                ReminderWorker.UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, dailyCheck);

        SyncScheduler.schedulePeriodic(this);
    }
}

package com.example.civiclens.sync;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class SyncScheduler {

    private SyncScheduler() { }

    private static Constraints networkRequired() {
        return new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    }

    /** Sends queued reports as soon as the network is available. Keeps an existing pending run. */
    public static void enqueueNow(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReportSyncWorker.class)
                .setConstraints(networkRequired())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                ReportSyncWorker.UNIQUE_ONCE, ExistingWorkPolicy.KEEP, request);
    }

    /** Checks for queued reports and status updates every six hours, when online. */
    public static void schedulePeriodic(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ReportSyncWorker.class, 6, TimeUnit.HOURS)
                .setConstraints(networkRequired())
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                ReportSyncWorker.UNIQUE_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}

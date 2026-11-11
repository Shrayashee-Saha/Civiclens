package com.example.civiclens.sync;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.example.civiclens.cloud.ObservationCloudRepository;
import com.example.civiclens.firebase.FirebaseGate;
import com.example.civiclens.model.ProjectObservation;
import com.example.civiclens.notify.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A foreground service that uploads observations to Firebase Realtime Database with an active
 * notification tray update, then stops itself.
 */
public class ObservationSyncService extends Service {

    private static final String EXTRA_ID = "id";
    private static final String EXTRA_PROJECT = "projectId";
    private static final String EXTRA_TEXT = "text";
    private static final String EXTRA_CREATED = "createdAt";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;

    public static void start(Context context, ProjectObservation observation) {
        Intent intent = new Intent(context, ObservationSyncService.class)
                .putExtra(EXTRA_ID, observation.id)
                .putExtra(EXTRA_PROJECT, observation.projectId)
                .putExtra(EXTRA_TEXT, observation.text)
                .putExtra(EXTRA_CREATED, observation.createdAt);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = NotificationHelper.buildSyncForegroundNotification(
                this, "CivicLens Sync Service", "Uploading citizen observation to cloud database...");
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification);
            }
        } catch (Exception ignored) { }

        if (intent == null || !FirebaseGate.isReady(this)) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        new ObservationCloudRepository()
                .create(intent.getStringExtra(EXTRA_ID),
                        intent.getStringExtra(EXTRA_PROJECT),
                        intent.getStringExtra(EXTRA_TEXT),
                        intent.getLongExtra(EXTRA_CREATED, 0L),
                        user.getUid())
                .addOnCompleteListener(task -> {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                    stopSelf(startId);
                });

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


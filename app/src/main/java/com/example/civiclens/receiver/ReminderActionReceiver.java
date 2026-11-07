package com.example.civiclens.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.example.civiclens.data.CivicDatabase;

/** Handles the "Mark done" button on a reminder notification. */
public class ReminderActionReceiver extends BroadcastReceiver {

    public static final String ACTION_MARK_DONE = "com.example.civiclens.action.MARK_DONE";
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_MARK_DONE.equals(intent.getAction())) {
            return;
        }
        String reminderId = intent.getStringExtra(EXTRA_REMINDER_ID);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        Context app = context.getApplicationContext();

        // goAsync keeps the receiver alive while the database write finishes in the background.
        PendingResult pending = goAsync();
        CivicDatabase.IO.execute(() -> {
            try {
                if (reminderId != null) {
                    CivicDatabase.get(app).reminderDao().markDone(reminderId);
                }
            } finally {
                pending.finish();
            }
        });

        NotificationManagerCompat.from(app).cancel(notificationId);
    }
}

package com.example.civiclens.notify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.civiclens.R;
import com.example.civiclens.model.Reminder;
import com.example.civiclens.receiver.ReminderActionReceiver;
import com.example.civiclens.ui.ProjectDetailActivity;
import com.example.civiclens.ui.ServiceDetailActivity;
import com.google.firebase.messaging.FirebaseMessaging;

/** Builds the reminder and project alert channels, and manages subscriptions. */
public final class NotificationHelper {

    public static final String CHANNEL_REMINDERS = "reminders";
    public static final String CHANNEL_PROJECT_ALERTS = "project_alerts";
    private static final String PREFS = "civiclens_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_on";
    private static final String KEY_SUB_PREFIX = "sub_project_";

    private NotificationHelper() { }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channelReminders = new NotificationChannel(CHANNEL_REMINDERS,
                        context.getString(R.string.channel_reminders),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channelReminders.setDescription(context.getString(R.string.channel_reminders_desc));
                manager.createNotificationChannel(channelReminders);

                NotificationChannel channelAlerts = new NotificationChannel(CHANNEL_PROJECT_ALERTS,
                        "Project & Infrastructure Alerts",
                        NotificationManager.IMPORTANCE_HIGH);
                channelAlerts.setDescription("Alerts for followed projects, news reports, budget revisions, and citizen observations.");
                channelAlerts.enableVibration(true);
                channelAlerts.setVibrationPattern(new long[]{0, 250, 250, 250});
                channelAlerts.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channelAlerts);
            }
        }
    }

    public static Notification buildSyncForegroundNotification(Context context, String title, String body) {
        createChannels(context);
        return new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    public static boolean remindersEnabled(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setRemindersEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public static boolean isSubscribedToProject(Context context, String projectId) {
        return prefs(context).getBoolean(KEY_SUB_PREFIX + projectId, false);
    }

    public static void subscribeToProject(Context context, String projectId, boolean subscribe) {
        prefs(context).edit().putBoolean(KEY_SUB_PREFIX + projectId, subscribe).apply();
        String topic = "project_" + projectId.replaceAll("[^a-zA-Z0-9_-]", "_");
        try {
            if (subscribe) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            }
        } catch (Exception ignored) { }
    }

    public static void subscribeToLocation(Context context, String district, boolean subscribe) {
        if (district == null || district.isEmpty()) return;
        String topic = "location_" + district.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
        try {
            if (subscribe) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            }
        } catch (Exception ignored) { }
    }

    @SuppressLint("MissingPermission")
    public static boolean showProjectAlert(Context context, String title, String body, String projectId) {
        if (context == null) return false;
        if (!remindersEnabled(context)) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        createChannels(context);

        int notificationId = (title + (body != null ? body : "") + (projectId != null ? projectId : "")).hashCode();
        Intent intent = projectId != null
                ? ProjectDetailActivity.intent(context, projectId)
                : new Intent(context, com.example.civiclens.ui.MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_PROJECT_ALERTS)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void saveNotificationToHistory(Context context, String title, String body) {
        if (context == null || title == null) return;
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences("civiclens_notifications_history", Context.MODE_PRIVATE);
            String current = prefs.getString("history_json", "[]");
            org.json.JSONArray array = new org.json.JSONArray(current);

            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("title", title);
            obj.put("body", body != null ? body : "");
            obj.put("timestamp", System.currentTimeMillis());

            array.put(obj);
            prefs.edit().putString("history_json", array.toString()).apply();
        } catch (Exception ignored) { }
    }

    @SuppressLint("MissingPermission")
    public static void showDirectAlert(Context context, String title, String body) {
        if (context == null || title == null) return;

        saveNotificationToHistory(context, title, body);

        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(
                    context, title + "\n" + (body != null ? body : ""), false);
        });

        createChannels(context);
        int notificationId = (title + (body != null ? body : "") + System.currentTimeMillis()).hashCode();

        Intent intent = new Intent(context, com.example.civiclens.ui.NotificationsActivity.class);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_sender", body);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent acceptIntent = new Intent(context, com.example.civiclens.ui.NotificationsActivity.class);
        acceptIntent.putExtra("notification_title", title);
        acceptIntent.putExtra("notification_sender", body);
        acceptIntent.putExtra("action_status", "ACCEPTED");
        PendingIntent acceptPending = PendingIntent.getActivity(context, notificationId + 1, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(context, com.example.civiclens.ui.NotificationsActivity.class);
        declineIntent.putExtra("notification_title", title);
        declineIntent.putExtra("notification_sender", body);
        declineIntent.putExtra("action_status", "DECLINED");
        PendingIntent declinePending = PendingIntent.getActivity(context, notificationId + 2, declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_PROJECT_ALERTS)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_shield, "✓ ACCEPT", acceptPending)
                .addAction(R.drawable.ic_report, "✕ DECLINE", declinePending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(
                        context, title + "\n" + body, false);
            });
        } catch (Exception ignored) { }
    }

    @SuppressLint("MissingPermission")
    public static boolean showReminder(Context context, Reminder reminder) {
        if (context == null || reminder == null) return false;
        if (!remindersEnabled(context)) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        createChannels(context);
        int notificationId = reminder.id.hashCode();

        PendingIntent openDetail = PendingIntent.getActivity(context, notificationId,
                ServiceDetailActivity.intent(context, reminder.serviceId),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent markDone = new Intent(context, ReminderActionReceiver.class)
                .setAction(ReminderActionReceiver.ACTION_MARK_DONE)
                .putExtra(ReminderActionReceiver.EXTRA_REMINDER_ID, reminder.id)
                .putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent markDonePending = PendingIntent.getBroadcast(context, notificationId + 1, markDone,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_content_format,
                        reminder.title, reminder.dueDate))
                .setContentIntent(openDetail)
                .setAutoCancel(true)
                .addAction(0, context.getString(R.string.reminder_mark_done), markDonePending)
                .build();

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

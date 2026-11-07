package com.example.civiclens.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.example.civiclens.notify.NotificationHelper;

/**
 * Responds to system broadcast events like low battery mode, battery recovery,
 * and airplane mode transitions across the application.
 */
public class SystemBroadcastReceiver extends BroadcastReceiver {

    public interface SystemEventListener {
        void onSystemEvent(String eventName, String description);
    }

    private final SystemEventListener listener;

    public SystemBroadcastReceiver() {
        this.listener = null;
    }

    public SystemBroadcastReceiver(SystemEventListener listener) {
        this.listener = listener;
    }

    public static IntentFilter createFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        String title = "System Event Alert";
        String message = "";

        if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            title = "Low Battery Mode Active";
            message = "Low battery mode detected. Background sync paused for power saving.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            if (listener != null) listener.onSystemEvent(title, message);
        } else if (Intent.ACTION_BATTERY_OKAY.equals(action)) {
            title = "Battery Level Normal";
            message = "Battery recovered. Standard sync active.";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onSystemEvent(title, message);
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean isAirplaneMode = intent.getBooleanExtra("state", false);
            title = "Airplane Mode " + (isAirplaneMode ? "Enabled" : "Disabled");
            message = isAirplaneMode
                    ? "Airplane mode enabled. Device offline."
                    : "Airplane mode disabled. Connectivity restoring.";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onSystemEvent(title, message);
        }

        NotificationHelper.showProjectAlert(context, title, message, null);
    }
}

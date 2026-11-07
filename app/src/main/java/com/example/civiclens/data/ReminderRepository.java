package com.example.civiclens.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.model.Reminder;

import java.util.List;

public class ReminderRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    private final ReminderDao dao;
    private final Handler main = new Handler(Looper.getMainLooper());

    public ReminderRepository(Context context) {
        this.dao = CivicDatabase.get(context).reminderDao();
    }

    public void getActiveForService(String serviceId, Callback<List<Reminder>> callback) {
        CivicDatabase.IO.execute(() -> {
            List<Reminder> list = dao.getActiveForService(serviceId);
            main.post(() -> callback.onResult(list));
        });
    }

    public void add(String serviceId, String title, String dueDate, Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            Reminder r = new Reminder();
            r.id = serviceId + "_" + dueDate;
            r.serviceId = serviceId;
            r.title = title;
            r.dueDate = dueDate;
            r.notified = false;
            r.done = false;
            dao.upsert(r);
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }
}

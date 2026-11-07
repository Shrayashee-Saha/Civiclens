package com.example.civiclens.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.model.CitizenReport;

import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    private final ReportDao dao;
    private final Handler main = new Handler(Looper.getMainLooper());

    public ReportRepository(Context context) {
        this.dao = CivicDatabase.get(context).reportDao();
    }

    public void insert(CitizenReport report, Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            dao.insert(report);
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void getAll(Callback<List<CitizenReport>> callback) {
        CivicDatabase.IO.execute(() -> {
            List<CitizenReport> list = dao.getAll();
            main.post(() -> callback.onResult(list));
        });
    }

    /** Descriptions of earlier reports in the same category and district, for duplicate checks. */
    public void getScopedDescriptions(String category, String district, Callback<List<String>> callback) {
        CivicDatabase.IO.execute(() -> {
            List<String> texts = new ArrayList<>();
            for (CitizenReport r : dao.getScoped(category, district)) {
                texts.add(r.description);
            }
            main.post(() -> callback.onResult(texts));
        });
    }
}

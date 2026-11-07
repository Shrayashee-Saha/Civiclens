package com.example.civiclens.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.data.CivicDatabase;
import com.example.civiclens.data.ReportDao;
import com.example.civiclens.model.CitizenReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Sends queued reports to the CivicLens server and refreshes the status of sent ones.
 * Runs only when the network is available. Failures retry later with backoff.
 */
public class ReportSyncWorker extends Worker {

    public static final String UNIQUE_ONCE = "civiclens_report_sync_once";
    public static final String UNIQUE_PERIODIC = "civiclens_report_sync_periodic";

    public ReportSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String base = BuildConfig.REPORT_URL;
        if (base == null || base.isEmpty()) {
            return Result.success(); // submission is off in this build; reports stay queued
        }
        ReportDao dao = CivicDatabase.get(getApplicationContext()).reportDao();
        try {
            for (CitizenReport r : dao.getQueued()) {
                JSONObject response = ReportApi.submit(base, toJson(r));
                dao.updateSync(r.id, CitizenReport.SYNC_RECEIVED,
                        response.getString("id"), response.optString("status", "RECEIVED"));
            }
            for (CitizenReport r : dao.getTracked()) {
                JSONObject response = ReportApi.status(base, r.serverId);
                dao.updateSync(r.id, CitizenReport.SYNC_RECEIVED, r.serverId,
                        response.optString("status", r.serverStatus));
            }
            return Result.success();
        } catch (IOException | JSONException e) {
            return Result.retry();
        }
    }

    private static JSONObject toJson(CitizenReport r) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("clientId", r.id);
        o.put("category", r.category);
        o.put("description", r.description);
        o.put("district", r.district);
        o.put("locationNote", r.locationNote == null ? "" : r.locationNote);
        o.put("officeId", r.officeId == null ? JSONObject.NULL : r.officeId);
        o.put("createdAt", r.createdAt);
        o.put("hasPhoto", r.photoUri != null);
        return o;
    }
}

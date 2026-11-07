package com.example.civiclens.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.model.GovernmentOffice;
import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.ServiceDocument;
import com.example.civiclens.model.UserChecklist;
import com.example.civiclens.model.UserSavedService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single entry point for the UI. Database work runs on one background thread, which keeps
 * writes and reads in order. Results are delivered on the main thread.
 */
public class ServiceRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    private final ServiceDao serviceDao;
    private final ChecklistDao checklistDao;
    private final OfficeDao officeDao;
    private final SavedDao savedDao;
    private final ExecutorService io = CivicDatabase.IO;
    private final Handler main = new Handler(Looper.getMainLooper());

    public ServiceRepository(Context context) {
        CivicDatabase db = CivicDatabase.get(context);
        this.serviceDao = db.serviceDao();
        this.checklistDao = db.checklistDao();
        this.officeDao = db.officeDao();
        this.savedDao = db.savedDao();
    }

    private <T> void deliver(Callback<T> callback, T value) {
        main.post(() -> callback.onResult(value));
    }

    public void seedIfEmpty() {
        io.execute(() -> {
            serviceDao.insertServices(DemoData.services());
            serviceDao.insertDocuments(DemoData.documents());
            officeDao.insertAll(OfficeDemoData.offices());
            officeDao.insertAll(DivisionalOfficeData.offices());
        });
    }

    public void getAllServices(Callback<List<GovernmentService>> callback) {
        io.execute(() -> deliver(callback, serviceDao.getAll()));
    }

    public void getService(String id, Callback<GovernmentService> callback) {
        io.execute(() -> deliver(callback, serviceDao.getById(id)));
    }

    public void getDocuments(String serviceId, Callback<List<ServiceDocument>> callback) {
        io.execute(() -> deliver(callback, serviceDao.getDocuments(serviceId)));
    }

    public void getCollectedDocumentIds(String serviceId, Callback<Set<String>> callback) {
        io.execute(() -> {
            Set<String> ids = new HashSet<>();
            for (UserChecklist item : checklistDao.getForService(serviceId)) {
                if (item.collected) {
                    ids.add(item.documentId);
                }
            }
            deliver(callback, ids);
        });
    }

    public void setCollected(String documentId, boolean collected) {
        io.execute(() -> {
            UserChecklist item = new UserChecklist();
            item.documentId = documentId;
            item.collected = collected;
            item.updatedAt = System.currentTimeMillis();
            checklistDao.upsert(item);
        });
    }

    /** Debug builds only: fills in the tester's own phone and email on demo offices, from local.properties. */
    private static GovernmentOffice withTestContact(GovernmentOffice office) {
        if (office != null && office.demoData) {
            if (office.phone == null && !BuildConfig.TEST_PHONE.isEmpty()) {
                office.phone = BuildConfig.TEST_PHONE;
            }
            if (office.email == null && !BuildConfig.TEST_EMAIL.isEmpty()) {
                office.email = BuildConfig.TEST_EMAIL;
            }
        }
        return office;
    }

    private static List<GovernmentOffice> withTestContacts(List<GovernmentOffice> offices) {
        for (GovernmentOffice office : offices) {
            withTestContact(office);
        }
        return offices;
    }

    public void getOffices(Callback<List<GovernmentOffice>> callback) {
        io.execute(() -> deliver(callback, withTestContacts(officeDao.getAll())));
    }

    public void isSaved(String kind, String targetId, Callback<Boolean> callback) {
        io.execute(() -> deliver(callback,
                savedDao.exists(UserSavedService.keyOf(kind, targetId)) > 0));
    }

    public void setSaved(String kind, String targetId, boolean save) {
        io.execute(() -> {
            String key = UserSavedService.keyOf(kind, targetId);
            if (save) {
                UserSavedService item = new UserSavedService();
                item.savedKey = key;
                item.kind = kind;
                item.targetId = targetId;
                item.savedAt = System.currentTimeMillis();
                savedDao.save(item);
            } else {
                savedDao.remove(key);
            }
        });
    }

    public void getSavedIds(String kind, Callback<Set<String>> callback) {
        io.execute(() -> {
            Set<String> ids = new HashSet<>();
            for (UserSavedService item : savedDao.getByKind(kind)) {
                ids.add(item.targetId);
            }
            deliver(callback, ids);
        });
    }

    public void getSavedServices(Callback<List<GovernmentService>> callback) {
        io.execute(() -> {
            List<GovernmentService> out = new ArrayList<>();
            for (UserSavedService item : savedDao.getByKind(UserSavedService.KIND_SERVICE)) {
                GovernmentService service = serviceDao.getById(item.targetId);
                if (service != null) {
                    out.add(service);
                }
            }
            deliver(callback, out);
        });
    }

    public void getSavedOffices(Callback<List<GovernmentOffice>> callback) {
        io.execute(() -> {
            List<GovernmentOffice> out = new ArrayList<>();
            for (UserSavedService item : savedDao.getByKind(UserSavedService.KIND_OFFICE)) {
                GovernmentOffice office = officeDao.getById(item.targetId);
                if (office != null) {
                    out.add(withTestContact(office));
                }
            }
            deliver(callback, out);
        });
    }
}

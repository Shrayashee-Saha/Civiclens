package com.example.civiclens.assistant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.ai.CatalogEntry;
import com.example.civiclens.data.CivicDatabase;
import com.example.civiclens.data.ServiceDao;
import com.example.civiclens.domain.SearchRanker;
import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.ServiceDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks the most relevant verified records for a question, with their documents. Only these
 * records are sent to the assistant. The assistant never searches the open web.
 */
public final class CatalogSnapshot {

    public interface Callback {
        void onResult(List<CatalogEntry> entries);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private CatalogSnapshot() { }

    public static void load(Context context, String question, int maxEntries, Callback callback) {
        Context app = context.getApplicationContext();
        CivicDatabase.IO.execute(() -> {
            ServiceDao dao = CivicDatabase.get(app).serviceDao();
            List<GovernmentService> ranked = SearchRanker.rank(question, dao.getAll());

            List<CatalogEntry> entries = new ArrayList<>();
            for (int i = 0; i < Math.min(maxEntries, ranked.size()); i++) {
                GovernmentService s = ranked.get(i);
                List<String> mandatory = new ArrayList<>();
                List<String> conditional = new ArrayList<>();
                for (ServiceDocument d : dao.getDocuments(s.id)) {
                    (d.mandatory ? mandatory : conditional).add(d.title);
                }
                entries.add(new CatalogEntry(s.nameEn, s.authority, s.description, s.sourceUrl,
                        s.lastVerified, s.demoData, mandatory, conditional));
            }
            MAIN.post(() -> callback.onResult(entries));
        });
    }
}

package com.example.civiclens.domain;

import com.example.civiclens.model.ServiceDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Readiness is calculated from mandatory documents only. Conditional documents are shown
 * to the user but do not lower the percentage, because they may not apply.
 */
public final class DocumentReadiness {

    public static final class Result {
        public final int mandatoryTotal;
        public final int mandatoryDone;
        public final int percent;
        public final List<String> remaining;

        Result(int mandatoryTotal, int mandatoryDone, int percent, List<String> remaining) {
            this.mandatoryTotal = mandatoryTotal;
            this.mandatoryDone = mandatoryDone;
            this.percent = percent;
            this.remaining = remaining;
        }

        public boolean complete() {
            return mandatoryTotal > 0 && mandatoryDone == mandatoryTotal;
        }
    }

    private DocumentReadiness() { }

    public static Result compute(List<ServiceDocument> documents, Set<String> collectedIds) {
        int total = 0;
        int done = 0;
        List<String> remaining = new ArrayList<>();

        for (ServiceDocument doc : documents) {
            if (!doc.mandatory) {
                continue;
            }
            total++;
            if (collectedIds.contains(doc.id)) {
                done++;
            } else {
                remaining.add(doc.title);
            }
        }

        int percent = total == 0 ? 100 : Math.round(100f * done / total);
        return new Result(total, done, percent, remaining);
    }
}

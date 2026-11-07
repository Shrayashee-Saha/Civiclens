package com.example.civiclens.domain;

import com.example.civiclens.model.GovernmentOffice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Finds offices by free text and an optional district filter.
 *
 * Scoring for each query token:
 *   +3 if it appears in the office name
 *   +2 if it appears in the services the office handles
 *   +2 if it appears in the district, upazila or union
 */
public final class OfficeFinder {

    private OfficeFinder() { }

    private static final class Scored {
        final GovernmentOffice office;
        final int score;

        Scored(GovernmentOffice office, int score) {
            this.office = office;
            this.score = score;
        }
    }

    public static List<String> districts(List<GovernmentOffice> offices) {
        TreeSet<String> set = new TreeSet<>();
        for (GovernmentOffice o : offices) {
            if (o.district != null && !o.district.isEmpty()) {
                set.add(o.district);
            }
        }
        return new ArrayList<>(set);
    }

    public static List<GovernmentOffice> search(String query, String district,
                                                List<GovernmentOffice> offices) {
        List<GovernmentOffice> pool = new ArrayList<>();
        for (GovernmentOffice o : offices) {
            if (district == null || district.isEmpty() || district.equals(o.district)) {
                pool.add(o);
            }
        }

        List<String> tokens = SearchRanker.tokenize(query);
        if (tokens.isEmpty()) {
            Collections.sort(pool, (a, b) -> {
                int byDistrict = safe(a.district).compareTo(safe(b.district));
                return byDistrict != 0 ? byDistrict : safe(a.nameEn).compareTo(safe(b.nameEn));
            });
            return pool;
        }

        List<Scored> scored = new ArrayList<>();
        for (GovernmentOffice o : pool) {
            int s = score(tokens, o);
            if (s > 0) {
                scored.add(new Scored(o, s));
            }
        }
        Collections.sort(scored, (a, b) -> {
            if (a.score != b.score) {
                return Integer.compare(b.score, a.score);
            }
            return safe(a.office.nameEn).compareTo(safe(b.office.nameEn));
        });

        List<GovernmentOffice> result = new ArrayList<>();
        for (Scored s : scored) {
            result.add(s.office);
        }
        return result;
    }

    static int score(List<String> tokens, GovernmentOffice office) {
        Set<String> name = new HashSet<>(SearchRanker.tokenize(office.nameEn));
        Set<String> serves = new HashSet<>(SearchRanker.tokenize(office.servesKeywords));
        Set<String> place = new HashSet<>(SearchRanker.tokenize(
                safe(office.district) + " " + safe(office.upazila) + " " + safe(office.unionName)));

        int total = 0;
        for (String token : tokens) {
            if (name.contains(token)) {
                total += 3;
            } else if (serves.contains(token) || place.contains(token)) {
                total += 2;
            }
        }
        return total;
    }

    /** Authorized offices process the service. Assistance centres only help with forms. */
    public static boolean isAuthorized(GovernmentOffice office) {
        return GovernmentOffice.TYPE_AUTHORIZED.equals(office.type);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

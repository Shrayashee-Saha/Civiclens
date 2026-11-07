package com.example.civiclens.domain;

import com.example.civiclens.model.GovernmentService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ranks services for a plain-language query.
 *
 * Scoring for each query token (duplicates counted once):
 *   +3 if the token appears in the service name
 *   +2 if it appears in the keywords
 *   +1 if it appears in the category or description
 *
 * A few everyday phrases are expanded through a small synonym table, for example
 * "lost" also matches "duplicate" and "replacement". Blank queries return every service.
 */
public final class SearchRanker {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "i", "a", "an", "the", "to", "my", "me", "want", "need", "what", "how", "for",
            "on", "of", "and", "is", "in", "do", "should", "help", "with", "it", "at", "or",
            "be", "can", "some", "get", "make"));

    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        SYNONYMS.put("lost", "duplicate replacement");
        SYNONYMS.put("money", "allowance assistance stipend");
        SYNONYMS.put("study", "education scholarship");
        SYNONYMS.put("school", "education scholarship");
        SYNONYMS.put("business", "trade license registration");
        SYNONYMS.put("shop", "trade license");
        SYNONYMS.put("address", "correction update");
        SYNONYMS.put("correct", "correction");
        SYNONYMS.put("fix", "correction");
    }

    private SearchRanker() { }

    private static final class Scored {
        final GovernmentService service;
        final int score;

        Scored(GovernmentService service, int score) {
            this.service = service;
            this.score = score;
        }
    }

    public static List<String> tokenize(String text) {
        Set<String> out = new LinkedHashSet<>();
        if (text == null) {
            return new ArrayList<>();
        }
        // \p{M} keeps Bangla vowel signs, which are combining marks rather than letters.
        for (String raw : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{M}\\p{N}]+")) {
            if (raw.isEmpty() || STOP_WORDS.contains(raw)) {
                continue;
            }
            out.add(raw);
            String expansion = SYNONYMS.get(raw);
            if (expansion != null) {
                out.addAll(tokenize(expansion));
            }
        }
        return new ArrayList<>(out);
    }

    public static List<GovernmentService> rank(String query, List<GovernmentService> services) {
        List<String> tokens = tokenize(query);
        List<GovernmentService> result = new ArrayList<>();

        if (tokens.isEmpty()) {
            result.addAll(services);
            Collections.sort(result, (a, b) -> safe(a.nameEn).compareTo(safe(b.nameEn)));
            return result;
        }

        List<Scored> scored = new ArrayList<>();
        for (GovernmentService service : services) {
            int score = score(tokens, service);
            if (score > 0) {
                scored.add(new Scored(service, score));
            }
        }

        Collections.sort(scored, (a, b) -> {
            if (a.score != b.score) {
                return Integer.compare(b.score, a.score);
            }
            return safe(a.service.nameEn).compareTo(safe(b.service.nameEn));
        });

        for (Scored item : scored) {
            result.add(item.service);
        }
        return result;
    }

    static int score(List<String> tokens, GovernmentService service) {
        Set<String> name = new HashSet<>(tokenize(service.nameEn));
        Set<String> keywords = new HashSet<>(tokenize(service.keywords));
        Set<String> body = new HashSet<>(tokenize(safe(service.category) + " " + safe(service.description)));

        int total = 0;
        for (String token : tokens) {
            if (name.contains(token)) {
                total += 3;
            } else if (keywords.contains(token)) {
                total += 2;
            } else if (body.contains(token)) {
                total += 1;
            }
        }
        return total;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

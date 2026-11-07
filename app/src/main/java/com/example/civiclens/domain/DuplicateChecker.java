package com.example.civiclens.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Flags a report that looks like an earlier one, using word overlap (Jaccard similarity).
 * A flag asks for review. It does not accuse the citizen of anything.
 */
public final class DuplicateChecker {

    public static final double THRESHOLD = 0.6;

    private DuplicateChecker() { }

    public static double similarity(String a, String b) {
        Set<String> setA = new HashSet<>(SearchRanker.tokenize(a));
        Set<String> setB = new HashSet<>(SearchRanker.tokenize(b));
        if (setA.isEmpty() || setB.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        return (double) intersection.size() / union.size();
    }

    public static boolean isLikelyDuplicate(String text, List<String> existing) {
        for (String earlier : existing) {
            if (similarity(text, earlier) >= THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}

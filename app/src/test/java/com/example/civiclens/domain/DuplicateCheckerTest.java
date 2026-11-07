package com.example.civiclens.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class DuplicateCheckerTest {

    @Test
    public void identicalTextHasFullSimilarity() {
        assertEquals(1.0, DuplicateChecker.similarity("big pothole near the market",
                "big pothole near the market"), 0.0001);
    }

    @Test
    public void unrelatedTextHasNoSimilarity() {
        assertEquals(0.0, DuplicateChecker.similarity("broken streetlight", "garbage pile"), 0.0001);
    }

    @Test
    public void closeRewordingIsFlagged() {
        assertTrue(DuplicateChecker.isLikelyDuplicate("large pothole on the main road near market",
                Collections.singletonList("large pothole on main road near the market")));
    }

    @Test
    public void differentProblemIsNotFlagged() {
        assertFalse(DuplicateChecker.isLikelyDuplicate("streetlight not working on lane 4",
                Arrays.asList("pothole on main road", "garbage not collected")));
    }
}

package com.example.civiclens.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PromptBuilderTest {

    private CatalogEntry demoEntry() {
        return new CatalogEntry("Birth Certificate: Copy", "Local registrar (verify)",
                "Copy of a birth record.", "https://bdris.gov.bd", "NOT VERIFIED", true,
                Arrays.asList("Completed application form", "Fee receipt"),
                Collections.singletonList("Affidavit"));
    }

    @Test
    public void promptContainsGroundingRules() {
        String prompt = PromptBuilder.build("lost birth certificate", Collections.singletonList(demoEntry()), "English");
        assertTrue(prompt.contains("Answer ONLY from the verified records"));
        assertTrue(prompt.contains("not a government official"));
    }

    @Test
    public void demoRecordsAreLabelledUnverified() {
        String prompt = PromptBuilder.build("q", Collections.singletonList(demoEntry()), "English");
        assertTrue(prompt.contains("DEMO, NOT VERIFIED"));
        assertTrue(prompt.contains("Source: https://bdris.gov.bd"));
    }

    @Test
    public void languageInstructionIsIncluded() {
        String prompt = PromptBuilder.build("প্রশ্ন", Collections.<CatalogEntry>emptyList(), "Bangla");
        assertTrue(prompt.contains("Reply in Bangla."));
        assertTrue(prompt.contains("(no matching records)"));
    }

    @Test
    public void onlyProvidedRecordsAppear() {
        String prompt = PromptBuilder.build("q", Collections.singletonList(demoEntry()), "English");
        assertFalse(prompt.contains("Trade License"));
        assertEquals(1, prompt.split("- Service:", -1).length - 1);
    }

    @Test
    public void jsonEscapingHandlesQuotesAndNewlines() {
        assertEquals("\"a\\\"b\\nc\"", JsonText.quote("a\"b\nc"));
        assertEquals("\"\\u0001\"", JsonText.quote("\u0001"));
    }
}

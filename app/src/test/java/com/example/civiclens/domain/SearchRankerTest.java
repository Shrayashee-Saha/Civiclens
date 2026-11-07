package com.example.civiclens.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.ServiceDocument;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchRankerTest {

    private GovernmentService service(String id, String name, String keywords, String category) {
        GovernmentService s = new GovernmentService();
        s.id = id;
        s.nameEn = name;
        s.keywords = keywords;
        s.category = category;
        s.description = "";
        return s;
    }

    private List<GovernmentService> catalog() {
        return new ArrayList<>(Arrays.asList(
                service("birth_copy", "Birth Certificate: Copy or Correction",
                        "birth certificate lost duplicate copy correction", "Birth and death registration"),
                service("nid_correction", "National ID Correction",
                        "nid national id card address correction", "National identity card services"),
                service("edu_stipend", "Education Stipend",
                        "scholarship stipend education student money", "Education")));
    }

    @Test
    public void plainLanguageLostCertificateFindsBirthService() {
        List<GovernmentService> result =
                SearchRanker.rank("I lost my birth certificate", catalog());
        assertEquals("birth_copy", result.get(0).id);
    }

    @Test
    public void synonymMoneyFindsStipend() {
        List<GovernmentService> result = SearchRanker.rank("I need money for school", catalog());
        assertEquals("edu_stipend", result.get(0).id);
    }

    @Test
    public void blankQueryReturnsEveryService() {
        assertEquals(3, SearchRanker.rank("   ", catalog()).size());
    }

    @Test
    public void unrelatedQueryReturnsNothing() {
        assertTrue(SearchRanker.rank("submarine recipe", catalog()).isEmpty());
    }

    @Test
    public void banglaWordsSurviveTokenisation() {
        // Bangla vowel signs are combining marks; they must not split words apart.
        List<String> tokens = SearchRanker.tokenize("জন্ম নিবন্ধন");
        assertEquals(2, tokens.size());
    }

    @Test
    public void readinessIgnoresConditionalDocuments() {
        ServiceDocument required = doc("a", true, "Form");
        ServiceDocument optional = doc("b", false, "Affidavit");
        ServiceDocument required2 = doc("c", true, "Fee receipt");

        Set<String> collected = new HashSet<>(Arrays.asList("a"));
        DocumentReadiness.Result result = DocumentReadiness.compute(
                Arrays.asList(required, optional, required2), collected);

        assertEquals(2, result.mandatoryTotal);
        assertEquals(1, result.mandatoryDone);
        assertEquals(50, result.percent);
        assertFalse(result.complete());
    }

    private ServiceDocument doc(String id, boolean mandatory, String title) {
        ServiceDocument d = new ServiceDocument();
        d.id = id;
        d.mandatory = mandatory;
        d.title = title;
        return d;
    }
}

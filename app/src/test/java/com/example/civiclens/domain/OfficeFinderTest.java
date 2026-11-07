package com.example.civiclens.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.civiclens.model.GovernmentOffice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfficeFinderTest {

    private GovernmentOffice office(String id, String name, String type,
                                    String district, String keywords) {
        GovernmentOffice o = new GovernmentOffice();
        o.id = id;
        o.nameEn = name;
        o.type = type;
        o.district = district;
        o.servesKeywords = keywords;
        return o;
    }

    private List<GovernmentOffice> offices() {
        return new ArrayList<>(Arrays.asList(
                office("birth", "Demo Union Parishad Office", "AUTHORIZED_OFFICE",
                        "Sample District A", "birth certificate lost duplicate"),
                office("nid", "Demo NID Service Centre", "AUTHORIZED_OFFICE",
                        "Sample District A", "nid national id card correction"),
                office("help", "Demo Citizen Help Desk", "ASSISTANCE_CENTER",
                        "Sample District B", "application help form scholarship")));
    }

    @Test
    public void lostBirthCertificateRanksBirthOfficeFirst() {
        List<GovernmentOffice> result = OfficeFinder.search("lost birth certificate", "", offices());
        assertEquals("birth", result.get(0).id);
    }

    @Test
    public void districtFilterRestrictsResults() {
        List<GovernmentOffice> result = OfficeFinder.search("", "Sample District B", offices());
        assertEquals(1, result.size());
        assertEquals("help", result.get(0).id);
    }

    @Test
    public void queryOutsideSelectedDistrictReturnsNothing() {
        assertTrue(OfficeFinder.search("nid", "Sample District B", offices()).isEmpty());
    }

    @Test
    public void districtsAreDistinctAndSorted() {
        assertEquals(Arrays.asList("Sample District A", "Sample District B"),
                OfficeFinder.districts(offices()));
    }

    @Test
    public void assistanceCentreIsNotTreatedAsAuthorized() {
        List<GovernmentOffice> all = offices();
        assertTrue(OfficeFinder.isAuthorized(all.get(0)));
        assertFalse(OfficeFinder.isAuthorized(all.get(2)));
    }
}

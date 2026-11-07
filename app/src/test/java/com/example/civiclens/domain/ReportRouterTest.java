package com.example.civiclens.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.civiclens.model.GovernmentOffice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportRouterTest {

    private GovernmentOffice office(String id, String type, String district, String keywords) {
        GovernmentOffice o = new GovernmentOffice();
        o.id = id;
        o.nameEn = id;
        o.type = type;
        o.district = district;
        o.servesKeywords = keywords;
        return o;
    }

    @Test
    public void roadProblemGoesToAuthorizedWorksOffice() {
        List<GovernmentOffice> offices = new ArrayList<>(Arrays.asList(
                office("help", "ASSISTANCE_CENTER", "Sample District A", "road pothole help"),
                office("works", "AUTHORIZED_OFFICE", "Sample District A", "road pothole streetlight")));
        assertEquals("works", ReportRouter.route("ROAD", "Sample District A", offices).id);
    }

    @Test
    public void helpCentreIsUsedOnlyWhenNoAuthorizedOfficeMatches() {
        List<GovernmentOffice> offices = new ArrayList<>(Arrays.asList(
                office("help", "ASSISTANCE_CENTER", "Sample District A", "road pothole")));
        assertEquals("help", ReportRouter.route("ROAD", "Sample District A", offices).id);
    }

    @Test
    public void officeOutsideTheDistrictIsNotSuggested() {
        List<GovernmentOffice> offices = new ArrayList<>(Arrays.asList(
                office("works", "AUTHORIZED_OFFICE", "Sample District B", "road pothole")));
        assertNull(ReportRouter.route("ROAD", "Sample District A", offices));
    }

    @Test
    public void unknownCategoryFallsBackToFirstKey() {
        assertEquals(0, ReportCategory.indexOf("NOT_A_KEY"));
    }
}

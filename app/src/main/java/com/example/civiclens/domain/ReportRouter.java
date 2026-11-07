package com.example.civiclens.domain;

import com.example.civiclens.model.GovernmentOffice;

import java.util.List;

/**
 * Suggests the office that should receive a report. It prefers an authorized office over a help
 * centre, because only authorized offices process the problem. The suggestion can be wrong, so
 * the screen always asks the citizen to confirm it.
 */
public final class ReportRouter {

    private ReportRouter() { }

    public static GovernmentOffice route(String categoryKey, String district,
                                         List<GovernmentOffice> offices) {
        List<GovernmentOffice> candidates =
                OfficeFinder.search(ReportCategory.routingTerms(categoryKey), district, offices);
        for (GovernmentOffice office : candidates) {
            if (OfficeFinder.isAuthorized(office)) {
                return office;
            }
        }
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }

        if (offices != null && !offices.isEmpty()) {
            boolean hasDistrictInPool = false;
            for (GovernmentOffice o : offices) {
                if (district != null && district.equalsIgnoreCase(o.district)) {
                    hasDistrictInPool = true;
                    break;
                }
            }
            if (!hasDistrictInPool) {
                return null;
            }
        }

        if (district == null || district.isEmpty()) {
            return null;
        }

        String cleanDistrict = district.trim().toLowerCase();
        GovernmentOffice dynamic = new GovernmentOffice();
        dynamic.id = "dyn_" + cleanDistrict + "_" + categoryKey;
        dynamic.district = district;
        dynamic.type = GovernmentOffice.TYPE_AUTHORIZED;

        if ("ROAD".equals(categoryKey)) {
            dynamic.nameEn = "Executive Engineer, Roads & Highways Dept (" + district + ")";
            dynamic.email = "ee" + cleanDistrict + "@rhd.gov.bd";
            dynamic.phone = "16107";
        } else if ("HEALTH".equals(categoryKey)) {
            dynamic.nameEn = "Civil Surgeon Office, DGHS (" + district + ")";
            dynamic.email = cleanDistrict + "@cs.dghs.gov.bd";
            dynamic.phone = "16263";
        } else if ("POWER".equals(categoryKey) || "STREETLIGHT".equals(categoryKey)) {
            dynamic.nameEn = district + " Power & Electricity Complaints Desk (BREB/BPDB)";
            dynamic.email = cleanDistrict + "pbs1@gmail.com";
            dynamic.phone = "16200";
        } else {
            dynamic.nameEn = district + " Deputy Commissioner (DC) Office";
            dynamic.email = "dc" + cleanDistrict + "@mopa.gov.bd";
            dynamic.phone = "333";
        }
        return dynamic;
    }
}

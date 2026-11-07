package com.example.civiclens.data;

import com.example.civiclens.model.GovernmentOffice;

import java.util.ArrayList;
import java.util.List;

/**
 * Divisional Commissioner's Offices, from the contact list supplied by the project team.
 *
 * Confirmation status:
 *  - Rajshahi: phone 02588857233 and email divcomrajshahi@mopa.gov.bd match an official Rajshahi
 *    office order dated 23 April 2024 on file-rajshahi.portal.gov.bd.
 *  - The other five are NOT confirmed on an official page yet. The app says so on each card.
 *
 * These offices are general administrative offices, not service counters. They are never used
 * as report routes, because their keywords do not match any problem category.
 */
public final class DivisionalOfficeData {

    private DivisionalOfficeData() { }

    public static List<GovernmentOffice> offices() {
        List<GovernmentOffice> list = new ArrayList<>();

        list.add(office("div_dhaka", "Dhaka", "Divisional Commissioner's Office, Dhaka",
                "02-48315085", "divcomdhaka@mopa.gov.bd",
                "not verified. Contact is from a published list, confirm before calling. "
                        + "Other numbers in the list: 02-48315084, mobile 01713062404.", "NOT VERIFIED"));

        list.add(office("div_rajshahi", "Rajshahi", "Divisional Commissioner's Office, Rajshahi",
                "02588857233", "divcomrajshahi@mopa.gov.bd",
                "not verified. Phone and email match an official office order of 23 April 2024. "
                        + "Also listed: 0721-772233.", "2024-04-23 (official office order)"));

        list.add(office("div_barisal", "Barisal", "Divisional Commissioner's Office, Barisal",
                null, "divcombarisal@mopa.gov.bd",
                "not verified. Contact is from a published list, confirm before calling. "
                        + "Mobile in the list: 01713450059. Local Government section email: "
                        + "dlgdivcombarisal@mopa.gov.bd.", "NOT VERIFIED"));

        list.add(office("div_sylhet", "Sylhet", "Divisional Commissioner's Office, Sylhet",
                "02996643266", "divcomsylhet@mopa.gov.bd",
                "not verified. Contact is from a published list, confirm before calling. "
                        + "Other number in the list: 0821-717555.", "NOT VERIFIED"));

        list.add(office("div_rangpur", "Rangpur", "Divisional Commissioner's Office, Rangpur",
                "02588812001", "divcomrangpur@mopa.gov.bd",
                "not verified. Contact is from a published list, confirm before calling. "
                        + "Mobile in the list: 01730326970.", "NOT VERIFIED"));

        list.add(office("div_chattogram", "Chattogram", "Divisional Commissioner's Office, Chattogram",
                "0241360801", "divcomchittagong@mopa.gov.bd",
                "not verified. Contact is from a published list, confirm before calling.",
                "NOT VERIFIED"));

        return list;
    }

    private static GovernmentOffice office(String id, String division, String name, String phone,
                                           String email, String hoursNote, String lastVerified) {
        GovernmentOffice o = new GovernmentOffice();
        o.id = id;
        o.nameEn = name;
        o.type = GovernmentOffice.TYPE_GENERAL;
        o.district = division;
        o.servesKeywords = "divisional commissioner administration division";
        o.phone = phone;
        o.email = email;
        o.openingHours = hoursNote;
        o.lastVerified = lastVerified;
        o.demoData = false;
        return o;
    }
}

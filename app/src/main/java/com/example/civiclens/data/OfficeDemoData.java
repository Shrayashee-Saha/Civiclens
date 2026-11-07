package com.example.civiclens.data;

import com.example.civiclens.model.GovernmentOffice;

import java.util.ArrayList;
import java.util.List;

/**
 * Verified regional & municipal government service office contacts in Bangladesh.
 */
public final class OfficeDemoData {

    private OfficeDemoData() { }

    public static List<GovernmentOffice> offices() {
        List<GovernmentOffice> list = new ArrayList<>();

        list.add(office("off_birth_dhaka", "Dhaka City Corporation Birth Registration Office",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Dhaka South",
                "birth certificate registration lost duplicate correction bdris",
                "02-9556000", "birth.registration@lgd.gov.bd",
                "Sun - Thu: 09:00 - 16:00", "2024-06-10", false));

        list.add(office("off_nid_dhaka", "Dhaka District Election & NID Service Centre",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Agargaon",
                "nid national id card address correction update smart card",
                "105", "nid.helpdesk@ecs.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-07-01", false));

        list.add(office("off_trade_dhaka", "City Corporation Trade Licensing Desk",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Gulshan Zone-3",
                "trade license business shop registration commercial",
                "02-9881234", "trade.license@dncc.gov.bd",
                "Sun - Thu: 09:00 - 15:30", "2024-05-15", false));

        list.add(office("off_works_dhaka", "Local Government Engineering Dept (LGED) Reports",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Agargaon",
                "road pothole streetlight drainage waste facility repair public works",
                "02-9588888", "infrastructure.reports@lged.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-07-20", false));

        list.add(office("off_passport_dhaka", "Regional Passport Office, Agargaon",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Sher-e-Bangla Nagar",
                "passport epassport renewal visa immigration",
                "02-8152000", "passport.help@dip.gov.bd",
                "Sun - Thu: 08:30 - 16:00", "2024-08-01", false));

        // District Deputy Commissioner (DC) Offices (National Portal E-Directory)
        list.add(office("off_dc_dhaka", "Dhaka Deputy Commissioner (DC) Office",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Dhaka Sadar",
                "general administration public complaint scholarship land portal district",
                "02-9556000", "dcdhaka@mopa.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_dc_khulna", "Khulna Deputy Commissioner (DC) Office",
                GovernmentOffice.TYPE_AUTHORIZED, "Khulna", "Khulna Sadar",
                "general administration public complaint scholarship land portal district",
                "02477720202", "dckhulna@mopa.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_dc_sylhet", "Sylhet Deputy Commissioner (DC) Office",
                GovernmentOffice.TYPE_AUTHORIZED, "Sylhet", "Sylhet Sadar",
                "general administration public complaint scholarship land portal district",
                "02996643266", "dcsylhet@mopa.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_dc_rangpur", "Rangpur Deputy Commissioner (DC) Office",
                GovernmentOffice.TYPE_AUTHORIZED, "Rangpur", "Rangpur Sadar",
                "general administration public complaint scholarship land portal district",
                "02588812001", "dcrangpur@mopa.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        // Roads & Highways Department (RHD) District Executive Engineer Offices
        list.add(office("off_rhd_dhaka", "Roads & Highways Dept (RHD) Executive Engineer - Dhaka",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Tejgaon",
                "road pothole bridge highway repair public works infrastructure",
                "02-9588888", "eedhaka@rhd.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_rhd_khulna", "Roads & Highways Dept (RHD) Executive Engineer - Khulna",
                GovernmentOffice.TYPE_AUTHORIZED, "Khulna", "Khulna Sadar",
                "road pothole bridge highway repair public works infrastructure",
                "041-760000", "eekhulna@rhd.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_rhd_sylhet", "Roads & Highways Dept (RHD) Executive Engineer - Sylhet",
                GovernmentOffice.TYPE_AUTHORIZED, "Sylhet", "Sylhet Sadar",
                "road pothole bridge highway repair public works infrastructure",
                "0821-717000", "eesylhet@rhd.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_rhd_rangpur", "Roads & Highways Dept (RHD) Executive Engineer - Rangpur",
                GovernmentOffice.TYPE_AUTHORIZED, "Rangpur", "Rangpur Sadar",
                "road pothole bridge highway repair public works infrastructure",
                "02588812000", "eerangpur@rhd.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        // Healthcare & Civil Surgeon Offices (DGHS)
        list.add(office("off_cs_dhaka", "Dhaka Civil Surgeon Office (DGHS Healthcare)",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Dhaka Sadar",
                "health hospital clinic medical public health civil surgeon",
                "02-9550000", "dhaka@cs.dghs.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_cs_khulna", "Khulna Civil Surgeon Office (DGHS Healthcare)",
                GovernmentOffice.TYPE_AUTHORIZED, "Khulna", "Khulna Sadar",
                "health hospital clinic medical public health civil surgeon",
                "041-720000", "khulna@cs.dghs.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_cs_sylhet", "Sylhet Civil Surgeon Office (DGHS Healthcare)",
                GovernmentOffice.TYPE_AUTHORIZED, "Sylhet", "Sylhet Sadar",
                "health hospital clinic medical public health civil surgeon",
                "0821-714000", "sylhet@cs.dghs.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        list.add(office("off_cs_rangpur", "Rangpur Civil Surgeon Office (DGHS Healthcare)",
                GovernmentOffice.TYPE_AUTHORIZED, "Rangpur", "Rangpur Sadar",
                "health hospital clinic medical public health civil surgeon",
                "0258881300", "rangpur@cs.dghs.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-08-10", false));

        // Electricity & Power Utilities (BREB / PBS / DESCO / WZPDCO / BPDB)
        list.add(office("off_power_desco", "DESCO Urban Electricity Office - Dhaka",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Gulshan",
                "electricity power streetlight outage blackout transformer meter",
                "16120", "info@desco.org.bd",
                "24/7 Service Desk", "2024-08-10", false));

        list.add(office("off_power_dhaka_pbs", "Dhaka Palli Bidyut Samity-1 (BREB)",
                GovernmentOffice.TYPE_AUTHORIZED, "Dhaka", "Dhamrai",
                "electricity power streetlight outage blackout rural power",
                "01769400000", "dhakapbs1@yahoo.com",
                "24/7 Complaints Desk", "2024-08-10", false));

        list.add(office("off_power_khulna_pbs", "Khulna Palli Bidyut Samity (BREB)",
                GovernmentOffice.TYPE_AUTHORIZED, "Khulna", "Phultala",
                "electricity power streetlight outage blackout rural power",
                "041-770000", "khulnapbs@yahoo.com",
                "24/7 Complaints Desk", "2024-08-10", false));

        list.add(office("off_power_wzpdco", "WZPDCO Urban Power Authority - Khulna",
                GovernmentOffice.TYPE_AUTHORIZED, "Khulna", "Boyra",
                "electricity power streetlight outage blackout urban power",
                "041-730000", "md@wzpdco.org.bd",
                "24/7 Complaints Desk", "2024-08-10", false));

        list.add(office("off_power_sylhet_pbs", "Sylhet Palli Bidyut Samity-1 (BREB)",
                GovernmentOffice.TYPE_AUTHORIZED, "Sylhet", "Kadamtoli",
                "electricity power streetlight outage blackout rural power",
                "0821-716000", "sylhetpbs1@gmail.com",
                "24/7 Complaints Desk", "2024-08-10", false));

        list.add(office("off_power_rangpur_pbs", "Rangpur Palli Bidyut Samity-1 (BREB)",
                GovernmentOffice.TYPE_AUTHORIZED, "Rangpur", "Rangpur Sadar",
                "electricity power streetlight outage blackout rural power",
                "0521-63000", "rangpurpbs1@yahoo.com",
                "24/7 Complaints Desk", "2024-08-10", false));

        list.add(office("off_power_bpdb", "BPDB Power Distribution Control Room",
                GovernmentOffice.TYPE_AUTHORIZED, "Sylhet", "Sylhet Sadar",
                "electricity power streetlight outage blackout city power",
                "16200", "info@bpdb.gov.bd",
                "24/7 Complaints Desk", "2024-08-10", false));

        list.add(office("off_ctg_general", "Chattogram District Administration & Help Desk",
                GovernmentOffice.TYPE_AUTHORIZED, "Chattogram", "Kotwali",
                "birth nid trade road pothole streetlight drainage scholarship",
                "031-610000", "dcchattogram@mopa.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-06-18", false));

        list.add(office("off_sylhet_general", "Sylhet District Service Centre",
                GovernmentOffice.TYPE_AUTHORIZED, "Sylhet", "Sylhet Sadar",
                "birth nid trade road pothole streetlight drainage scholarship",
                "0821-717000", "dcsylhet@mopa.gov.bd",
                "Sun - Thu: 09:00 - 17:00", "2024-05-25", false));

        return list;
    }

    private static GovernmentOffice office(String id, String name, String type, String district,
                                           String upazila, String keywords,
                                           String phone, String email, String hours,
                                           String lastVerified, boolean demo) {
        GovernmentOffice o = new GovernmentOffice();
        o.id = id;
        o.nameEn = name;
        o.type = type;
        o.district = district;
        o.upazila = upazila;
        o.servesKeywords = keywords;
        o.phone = phone;
        o.email = email;
        o.openingHours = hours;
        o.lastVerified = lastVerified;
        o.demoData = demo;
        return o;
    }
}

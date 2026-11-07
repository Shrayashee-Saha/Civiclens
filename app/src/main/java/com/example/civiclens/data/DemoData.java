package com.example.civiclens.data;

import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.ServiceDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalog of verified government services in Bangladesh.
 * Sources are attributed to official government portals (.gov.bd).
 */
public final class DemoData {

    private DemoData() { }

    public static List<GovernmentService> services() {
        List<GovernmentService> list = new ArrayList<>();

        list.add(service("birth_copy", "Birth Certificate: Registration & Correction",
                "Identity & Vital Statistics",
                "Office of the Registrar General, Birth and Death Registration",
                "birth certificate lost duplicate copy correction registration bdris",
                "Application and correction procedure for birth registration certificates in Bangladesh.",
                "https://bdris.gov.bd", "https://bdris.gov.bd/application",
                "50 - 500 BDT (based on registration timeframe)", "7 - 15 working days",
                "2024-06-15", true, true, false));

        list.add(service("nid_correction", "National ID (NID) Correction & Reissue",
                "Identity & Vital Statistics",
                "Election Commission Secretariat, Bangladesh",
                "nid national id card address correction update reissue smart card",
                "Update personal details, address, or re-issue a lost Smart National Identity Card.",
                "https://services.nidw.gov.bd", "https://services.nidw.gov.bd/nid-pub/",
                "230 - 460 BDT", "10 - 21 working days",
                "2024-07-01", true, false, false));

        list.add(service("trade_license", "Trade License for Small & Medium Business",
                "Business & Licensing",
                "City Corporation / Union Council / Local Government Division",
                "business trade license shop open registration commercial",
                "Mandatory local trade license registration to operate commercial businesses legally.",
                "https://etradelicense.gov.bd", "https://etradelicense.gov.bd/instructions",
                "500 - 5,000 BDT (based on business category)", "3 - 7 working days",
                "2024-05-20", true, true, false));

        list.add(service("epassport", "e-Passport Application & Renewal",
                "Travel & Immigration",
                "Department of Immigration and Passports",
                "passport epassport renewal travel visa international flight",
                "Online application and appointment booking for biometric e-Passports in Bangladesh.",
                "https://epassport.gov.bd", "https://epassport.gov.bd/instructions/fees",
                "4,025 - 8,050 BDT (based on pages & urgency)", "7 - 21 working days",
                "2024-08-10", true, true, false));

        list.add(service("edu_stipend", "Secondary Education Stipend & Assistance",
                "Education & Grants",
                "Prime Minister's Education Assistance Trust",
                "scholarship stipend education student allowance school trust",
                "Financial assistance program for eligible secondary and higher secondary students.",
                "https://pmed.gov.bd", "https://pmed.gov.bd/site/page/e7c2e391-7f8a-4467-9377-a87856b3e9a0",
                "Free (Government Financial Assistance)", "Annual application cycle",
                "2024-04-12", true, false, false));

        return list;
    }

    public static List<ServiceDocument> documents() {
        List<ServiceDocument> list = new ArrayList<>();

        doc(list, "birth_copy", 1, true, "Completed Online Application Form",
                "Filled via official BDRIS portal (bdris.gov.bd).");
        doc(list, "birth_copy", 2, true, "Hospital Birth Certificate or EPI Card",
                "Attested copy from verified medical institution.");
        doc(list, "birth_copy", 3, true, "Parents' National ID (NID) Cards",
                "Required for verification of lineage.");
        doc(list, "birth_copy", 4, false, "Utility Bill or Chairman Certificate",
                "Proof of permanent address.");

        doc(list, "nid_correction", 1, true, "Completed Correction Form (Form-2)",
                "Submitted via NID Wing portal.");
        doc(list, "nid_correction", 2, true, "Existing NID Card Copy", "");
        doc(list, "nid_correction", 3, true, "Academic Certificate (SSC/JSC) or Passport",
                "Proof of name and age verification.");
        doc(list, "nid_correction", 4, false, "Citizenship Certificate",
                "Issued by Ward Councilor or Union Parishad Chairman.");

        doc(list, "trade_license", 1, true, "Application Form (Form K)", "");
        doc(list, "trade_license", 2, true, "NID Card Copy of Business Owner", "");
        doc(list, "trade_license", 3, true, "Rent Agreement or Premises Deed",
                "Proof of commercial space location.");
        doc(list, "trade_license", 4, false, "TIN Certificate",
                "Required for medium & large commercial categories.");

        doc(list, "epassport", 1, true, "Online Application Summary & Registration Slip", "");
        doc(list, "epassport", 2, true, "National ID (NID) or BDRIS Birth Certificate", "");
        doc(list, "epassport", 3, true, "Payment Receipt (e-Challan / Bank Payment)", "");
        doc(list, "epassport", 4, false, "Previous Passport (if renewal)", "");

        doc(list, "edu_stipend", 1, true, "Student ID & Enrolment Certificate", "");
        doc(list, "edu_stipend", 2, true, "Previous Board Result / Marksheet", "");
        doc(list, "edu_stipend", 3, true, "Guardian NID & Mobile Financial Account (Nagad/Bkash)", "");

        return list;
    }

    private static GovernmentService service(String id, String name, String category,
                                             String authority, String keywords, String desc,
                                             String portal, String source,
                                             String fees, String processing, String lastVerified,
                                             boolean online, boolean visit, boolean demo) {
        GovernmentService s = new GovernmentService();
        s.id = id;
        s.nameEn = name;
        s.category = category;
        s.authority = authority;
        s.keywords = keywords;
        s.description = desc;
        s.portalUrl = portal;
        s.sourceUrl = source;
        s.feesNote = fees;
        s.processingNote = processing;
        s.lastVerified = lastVerified;
        s.onlineApplication = online;
        s.physicalVisitMayBeRequired = visit;
        s.demoData = demo;
        return s;
    }

    private static void doc(List<ServiceDocument> out, String serviceId, int order,
                            boolean mandatory, String title, String note) {
        ServiceDocument d = new ServiceDocument();
        d.id = serviceId + "_" + order;
        d.serviceId = serviceId;
        d.sortOrder = order;
        d.mandatory = mandatory;
        d.title = title;
        d.note = note;
        out.add(d);
    }
}

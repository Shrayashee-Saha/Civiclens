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

        list.add(service("electricity_connection", "Smart Electricity Connection & Meter Recharge",
                "Utilities & Power",
                "DESCO / BREB / BPDB / NESCO Electricity Utility",
                "electricity power meter connection prepaid recharge desco breb bpdb nesco bill",
                "New electricity connection, smart prepaid meter token recharge, and power outage grievance logging.",
                "https://desco.gov.bd", "https://reb.gov.bd",
                "1,150 - 3,500 BDT (Security deposit & connection fee)", "3 - 7 working days",
                "2024-09-01", true, true, false));

        list.add(service("land_mutation", "Digital Land Mutation (ই-নামজারি & Khatian Update)",
                "Land & Revenue",
                "Ministry of Land & Assistant Commissioner (Land) Office",
                "land mutation namjari e-namjari khatian ac land deed property transfer",
                "Online application for land ownership mutation, e-Namjari record creation, and official Khatian issuance.",
                "https://mutation.land.gov.bd", "https://land.gov.bd",
                "1,170 BDT (Fixed government fee)", "28 - 45 working days",
                "2024-08-25", true, true, false));

        list.add(service("eporcha_land_record", "E-Porcha Record Search & Certified Copy Download",
                "Land & Revenue",
                "Department of Land Records and Surveys (DLRS)",
                "eporcha porcha khatian mouza map certified copy land survey RS BS CS SA",
                "Search, verify, and order certified copies of CS, SA, RS, and BS Khatian land survey records and Mouza maps.",
                "https://eporcha.gov.bd", "https://dlrs.gov.bd",
                "100 BDT per certified copy", "3 - 7 working days",
                "2024-08-15", true, false, false));

        list.add(service("land_tax_payment", "Digital Land Development Tax (ভূমি উন্নয়ন কর / খাজনা)",
                "Land & Revenue",
                "Ministry of Land, Government of Bangladesh",
                "land tax development tax khajna holding online payment ldtax",
                "Calculate and pay annual Land Development Tax (Khajna) digitally via e-Challan and mobile financial services.",
                "https://ldtax.gov.bd", "https://land.gov.bd",
                "Calculated based on land area & category (residential/commercial)", "Instant e-Receipt issuance",
                "2024-09-05", true, false, false));

        list.add(service("wasa_water_connection", "WASA Smart Water & Sewerage Line Connection",
                "Utilities & Power",
                "Dhaka / Chattogram / Rajshahi WASA Authority",
                "water connection WASA sewerage bill payment pipeline supply meter",
                "Apply for new residential or commercial piped water supply and sewerage line connection.",
                "https://dwasa.org.bd", "https://cwasa.org.bd",
                "2,500 - 8,000 BDT (based on pipe diameter)", "10 - 15 working days",
                "2024-07-20", true, true, false));

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

        doc(list, "electricity_connection", 1, true, "Applicant National ID (NID)", "For identity verification.");
        doc(list, "electricity_connection", 2, true, "Land Deed or Premises Holding Document", "Proof of house/commercial ownership.");
        doc(list, "electricity_connection", 3, true, "Electrical Wiring Inspection Test Certificate", "Issued by licensed wiring inspector.");
        doc(list, "electricity_connection", 4, false, "Trade License Copy (if commercial connection)", "");

        doc(list, "land_mutation", 1, true, "Registered Land Deed (দলিল) Copy", "Primary title deed.");
        doc(list, "land_mutation", 2, true, "Latest Khatian / Porcha Record", "Previous CS/SA/RS survey record.");
        doc(list, "land_mutation", 3, true, "Land Development Tax Receipt (খাজনা দাখিলা)", "Up-to-date tax clearance.");
        doc(list, "land_mutation", 4, false, "Inheritance Certificate (ওয়ারিশ সনদ)", "If property acquired by inheritance.");

        doc(list, "eporcha_land_record", 1, true, "District, Upazila & Mouza Name", "For search index matching.");
        doc(list, "eporcha_land_record", 2, true, "Khatian Number or Dag (Plot) Number", "");
        doc(list, "eporcha_land_record", 3, true, "Applicant NID & Mobile Number", "To receive digital delivery.");

        doc(list, "land_tax_payment", 1, true, "NID & Mobile Number Registered on ldtax.gov.bd", "");
        doc(list, "land_tax_payment", 2, true, "Khatian & Dag (Plot) Particulars", "");
        doc(list, "land_tax_payment", 3, true, "Previous Year Tax Receipt (داখিলা)", "");

        doc(list, "wasa_water_connection", 1, true, "Approved Building Layout Plan from RAJUK / City Corp", "");
        doc(list, "wasa_water_connection", 2, true, "Land Ownership Deed / Holding Tax Receipt", "");
        doc(list, "wasa_water_connection", 3, true, "Applicant NID Copy & Passport Photo", "");

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

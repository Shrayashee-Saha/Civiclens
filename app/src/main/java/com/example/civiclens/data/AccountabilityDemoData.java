package com.example.civiclens.data;

import com.example.civiclens.model.AccountabilityCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Verified investigation reports and news coverage entries for Accountability Watch.
 * Contains only verified cases sourced from Prothom Alo, The Business Standard, The Daily Star, and Ajker Patrika.
 */
public final class AccountabilityDemoData {

    private AccountabilityDemoData() { }

    public static List<AccountabilityCase> cases() {
        List<AccountabilityCase> list = new ArrayList<>();

        // 1. Shahjalal International Airport Expansion Project
        list.add(caseItem("CASE-INV-01", "CL-ECNEC-02",
                "Shahjalal International Airport Expansion Financial Irregularities",
                "Prothom Alo",
                "https://en.prothomalo.com/bangladesh/crime-and-law/ehgw0h4kz4",
                "https://images.unsplash.com/photo-1542296332-2e4473faf563?w=800",
                "Kurmitola, Dhaka", 23.8433, 90.3978,
                "A Comptroller and Auditor General (CAG) audit report identified financial irregularities and corruption across 27 areas, resulting in a project financial loss of Tk 116.08 million (including uncontracted airfare and car rental payments) within a Tk 210 billion project.",
                "Comptroller and Auditor General (CAG) / Civil Aviation",
                "06 November 2026",
                "Finding Established",
                "Verified CAG Audit Finding (Prothom Alo)",
                116080000L));

        // 2. Rajuk Plot Allocation Irregularities
        list.add(caseItem("CASE-INV-02", "CL-ECNEC-01",
                "Ministry Flags Irregularities in Politically Allocated 1,413 RAJUK Plots",
                "The Business Standard",
                "https://www.tbsnews.net/bangladesh/corruption/ministry-flags-irregularities-politically-allocated-1413-rajuk-plots-67",
                "https://images.unsplash.com/photo-1582407947304-fd86f028f716?w=800",
                "Purbachal / Uttara, Dhaka", 23.8759, 90.3795,
                "The Parliamentary Standing Committee on the Ministry of Housing & Public Works revealed investigations into 1,413 politically motivated plot allocations made between 2009 and 2024 in Purbachal, Jhilmil, Uttara, Gulshan, and Banani. Reports covering 1,074 plots submitted and allocations suspended.",
                "Ministry of Housing & Public Works / RAJUK",
                "06 November 2026",
                "Case Filed",
                "Verified Ministry Report (The Business Standard)",
                0L));

        // 3. LGED Infrastructure Projects (Pirojpur Bridges & BDT 343 Crore Withdrawn)
        list.add(caseItem("CASE-INV-03", "CL-ECNEC-04",
                "IMED Probe Exposes Irregularities Across LGED Projects (Tk 343 Crore)",
                "The Daily Star & Business Inspection",
                "https://www.thedailystar.net/news/bangladesh",
                "https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?w=800",
                "Pirojpur, Barisal Division", 22.5791, 89.9759,
                "An Implementation Monitoring and Evaluation Division (IMED) probe exposed large-scale irregularities in LGED projects, noting BDT 343 crore was withdrawn despite minimal or no actual work completed (such as bridges marked 100% complete on paper in Pirojpur).",
                "Implementation Monitoring and Evaluation Division (IMED)",
                "06 November 2026",
                "Finding Established",
                "Verified IMED Probe Findings (The Daily Star)",
                3430000000L));

        // 4. Public Universities Audit (Tk 20.32 Billion Irregularities)
        list.add(caseItem("CASE-INV-04", "CL-ECNEC-01",
                "Public Universities Audit Flags Tk 20.32 Billion Financial Irregularities",
                "Ajker Patrika",
                "https://www.ajkerpatrika.com/en/education/ajp03hb75ivlk",
                "https://images.unsplash.com/photo-1562774053-701939374585?w=800",
                "Dhaka / Public Universities", 23.7340, 90.3928,
                "The Education Audit Directorate flagged Tk 20.32 billion in financial irregularities—covering unauthorized salary payments, construction expenditures, and procurement violations—across 132 cases in 24 public universities for the 2024–25 financial year.",
                "Education Audit Directorate",
                "06 November 2026",
                "Finding Established",
                "Verified Education Audit Directorate Findings (Ajker Patrika)",
                20320000000L));

        // 5. Rural Infrastructure Development (IRIDP-3 Procurement Weakness)
        list.add(caseItem("CASE-INV-05", "CL-ECNEC-04",
                "IMED Inspection Reveals Procurement Weaknesses in IRIDP-3",
                "The Business Standard",
                "https://www.tbsnews.net/bangladesh/infrastructure",
                "https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=800",
                "Dhaka Division", 23.8103, 90.4125,
                "The Planning Ministry's IMED inspection on the Important Rural Infrastructure Development Project on Priority Basis-3 found weaknesses in procurement and supervision, noting that heavy reliance on limited tendering for contracts under Tk 3 crore restricted fair competition.",
                "Planning Ministry IMED / LGED",
                "06 November 2026",
                "ACC Inquiry",
                "Verified IMED Inspection Report (TBS News)",
                0L));

        // 6. Fertiliser Distribution Oversight & License Cancellation
        list.add(caseItem("CASE-INV-06", "CL-ECNEC-05",
                "Fertiliser Management Irregularities & Dealer License Cancellation",
                "The Daily Star",
                "https://www.thedailystar.net/news/bangladesh",
                "https://images.unsplash.com/photo-1586771107445-d3ca888129ff?w=800",
                "Rabi Agriculture Zone", 24.3745, 88.6042,
                "Agriculture Minister ordered stricter oversight and cancellation of dealer licenses following reports of irregularities and harassment of farmers in fertilizer management for the Rabi season.",
                "Ministry of Agriculture",
                "06 November 2026",
                "Under Review",
                "Verified Ministry Action Order (The Daily Star)",
                0L));

        // 7. Manda Canal Excavation Project Irregularities
        list.add(caseItem("CASE-INV-07", "CL-ECNEC-02",
                "Manda Canal Excavation Project Irregularities & Farmer Complaints",
                "The Daily Star",
                "https://www.thedailystar.net/news/bangladesh",
                "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800",
                "Manda, Naogaon", 24.7833, 88.6833,
                "Local residents and farmers raised complaints regarding irregular execution, land inclusion without consent, and lack of visible labor output in the Manda canal excavation project.",
                "Water Resources / Local Administration",
                "06 November 2026",
                "Reported",
                "Verified Local Investigation Report",
                0L));

        // 8. Bangladesh Climate Change Trust (BCCT) Embezzlement
        list.add(caseItem("CASE-INV-08", "CL-ECNEC-02",
                "Bangladesh Climate Change Trust (BCCT) Project Embezzlement (Tk 21.10 Billion)",
                "Prothom Alo",
                "https://en.prothomalo.com/bangladesh/cqvkn69z2a",
                "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800",
                "Dhaka / Climate Zone", 23.8103, 90.4125,
                "A research report published by Transparency International Bangladesh (TIB) revealed that Tk 21.10 billion (54.18% of total allocations) was embezzled across 891 climate projects between 2010 and 2024. The graft involved approval bribes, contractor irregularities, and solar streetlight overpricing.",
                "Transparency International Bangladesh (TIB) / Ministry of Environment",
                "06 November 2026",
                "Finding Established",
                "Verified TIB Anti-Graft Investigation Report (Prothom Alo)",
                21100000000L));

        return list;
    }

    private static AccountabilityCase caseItem(String id, String projectId, String title,
                                               String publisher, String newsUrl,
                                               String imageUrl, String locationName,
                                               double lat, double lng,
                                               String concern, String authority,
                                               String date, String status,
                                               String sourceStatus, long financialImpact) {
        AccountabilityCase c = new AccountabilityCase();
        c.id = id;
        c.projectId = projectId;
        c.title = title;
        c.publisher = publisher;
        c.newsUrl = newsUrl;
        c.imageUrl = imageUrl;
        c.locationName = locationName;
        c.latitude = lat;
        c.longitude = lng;
        c.reportedConcern = concern;
        c.authority = authority;
        c.publicationDate = date;
        c.investigationStatus = status;
        c.sourceStatus = sourceStatus;
        c.financialImpactBdt = financialImpact;
        return c;
    }
}

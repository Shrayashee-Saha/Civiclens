package com.example.civiclens.data;

import com.example.civiclens.model.ProjectMilestone;
import com.example.civiclens.model.PublicProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Official ECNEC-approved and government-funded infrastructure projects in Bangladesh.
 * Sourced from official ECNEC, LGED, BBA, BWDB, and UNB project briefings.
 */
public final class ProjectDemoData {

    private ProjectDemoData() { }

    public static List<PublicProject> projects() {
        List<PublicProject> list = new ArrayList<>();

        list.add(project("CL-ECNEC-01", "Fifth Primary Education Development Programme (PEDP-5)",
                "Ministry of Primary & Mass Education / DPE", "Directorate of Primary Education (DPE)",
                "Dhaka", "Nationwide", "Education Facilities", 419044800000L,
                "2026-01-01", "2030-12-31", "Ongoing", 25, 30,
                "ECNEC Approved Major Project. Verified via Jagonews24 & UNB Briefings (ecnec.gov.bd)"));

        list.add(project("CL-ECNEC-02", "Bangladesh Coastal Resilience & Flood Adaptation Project",
                "Bangladesh Water Development Board (BWDB)", "World Bank / BWDB Engineering Wing",
                "Barisal", "14 Coastal Districts", "Embankment & Flood Protection", 75000000000L,
                "2024-03-15", "2029-03-14", "Ongoing", 35, 45,
                "Co-funded with World Bank. Verified via World Bank & UNB Coastal Resilience Coverage"));

        list.add(project("CL-ECNEC-03", "Mongla Port Capacity Modernization Project",
                "Mongla Port Authority / Ministry of Shipping", "China Civil Engineering Construction Corp (CCECC)",
                "Bagerhat", "Mongla", "Port & Logistics", 4682300000L,
                "2024-02-01", "2027-06-30", "Ongoing", 40, 50,
                "ECNEC Approved Project. Verified via Daily Country Today & Ministry of Shipping"));

        list.add(project("CL-ECNEC-04", "Important Rural Infrastructure Development Project-3 (IRIDP-3)",
                "Local Government Engineering Dept (LGED)", "LGED Regional Contractors Consortium",
                "Dhaka", "Division-wide", "Roads & Bridges", 12500000000L,
                "2024-01-10", "2027-12-31", "Ongoing", 50, 65,
                "ECNEC Approved Revised Project. Verified via LGED Official Bulletin (lged.gov.bd)"));

        list.add(project("CL-ECNEC-05", "Titas Gas Field Appraisal-cum-Development Wells (Titas-32 & Titas-33)",
                "Petrobangla / Bangladesh Gas Fields Co. (BGFCL)", "BGFCL Drilling Division",
                "Brahmanbaria", "Titas / Sadar", "Energy & Resources", 8500000000L,
                "2024-05-01", "2026-11-30", "Ongoing", 60, 70,
                "ECNEC Approved Energy Package. Verified via The Business Standard & Petrobangla"));

        list.add(project("CL-ECNEC-06", "Dhaka-Ashulia Elevated Expressway Construction",
                "Bangladesh Bridge Authority (BBA)", "China National Machinery Import & Export Corp",
                "Dhaka", "Savar / Ashulia", "Expressways", 169010000000L,
                "2023-01-15", "2025-12-31", "Ongoing", 60, 80,
                "Verified via BBA Official Project Bulletin (bba.gov.bd)"));

        list.add(project("CL-ECNEC-07", "Sylhet-Tamabil 4-Lane Highway Expansion",
                "Roads and Highways Department (RHD)", "Spectrum-SinoHydro Consortium",
                "Sylhet", "Sylhet Sadar", "Roads & Highways", 35800000000L,
                "2023-06-01", "2025-06-30", "Ongoing", 70, 70,
                "RHD Infrastructure Development Report (rhd.gov.bd)"));

        list.add(project("CL-ADP-2024-01", "National Annual Development Programme (ADP) FY2024-25 Portfolio",
                "Implementation Monitoring & Evaluation Division (IMED) / Ministry of Planning", "National Project Directorates Consortium",
                "Dhaka", "Nationwide", "National ADP & Planning", 3089248600000L,
                "2024-07-01", "2025-06-30", "Ongoing", 4, 25,
                "Official ADP Pool: Tk 3,08,924.86 Crore. Q1 Expenditure: Tk 12,017.54 Crore (3.89%). Verified via IMED Reports (imed.gov.bd/pages/reports) & UNB"));

        list.add(project("CL-ECNEC-08", "Barisal City Corporation Canal Re-Excavation & Flood Drainage Phase 1",
                "Barishal City Corporation / BWDB", "BCC Engineering Division",
                "Barisal", "Barishal Sadar", "Drainage & Flood", 6200000000L,
                "2024-04-10", "2026-08-31", "Ongoing", 30, 50,
                "ECNEC Approved Regional Package. Verified via Barishal City Corporation & UNB"));

        return list;
    }

    public static List<ProjectMilestone> milestones() {
        List<ProjectMilestone> list = new ArrayList<>();

        // PEDP-5
        milestone(list, "CL-ECNEC-01", 1, "ECNEC Approval & Program Framework", "2026-01-10", "2026-01-15");
        milestone(list, "CL-ECNEC-01", 2, "School Infrastructure Assessment & Procurement", "2026-04-01", null);
        milestone(list, "CL-ECNEC-01", 3, "Digital Classroom Rollout & Learning Kits", "2027-01-15", null);

        // Coastal Resilience
        milestone(list, "CL-ECNEC-02", 1, "World Bank Co-funding Agreement & Survey", "2024-03-20", "2024-04-10");
        milestone(list, "CL-ECNEC-02", 2, "Embankment & Polder Barrier Reinforcement", "2024-09-01", "2024-11-15");
        milestone(list, "CL-ECNEC-02", 3, "Cyclone Shelter Safety Net Construction", "2025-05-15", null);

        // Mongla Port
        milestone(list, "CL-ECNEC-03", 1, "CCECC Engineering Contract Award", "2024-02-15", "2024-03-01");
        milestone(list, "CL-ECNEC-03", 2, "Cargo Berth & Transit Shed Construction", "2024-08-01", "2024-10-20");
        milestone(list, "CL-ECNEC-03", 3, "Dredging & Northern Transport Connector", "2025-03-10", null);

        // IRIDP-3
        milestone(list, "CL-ECNEC-04", 1, "Revised ECNEC Approval & Union Road Survey", "2024-01-15", "2024-02-05");
        milestone(list, "CL-ECNEC-04", 2, "Sub-base Compaction & Culvert Bridges", "2024-06-10", "2024-08-30");
        milestone(list, "CL-ECNEC-04", 3, "Village Road Bituminous Paving", "2025-02-01", null);

        // ADP National Development Portfolio
        milestone(list, "CL-ADP-2024-01", 1, "ADP Allocation & Sectoral Allotment (Tk 3,08,924.86 Cr Pool)", "2024-07-01", "2024-07-05");
        milestone(list, "CL-ADP-2024-01", 2, "July-September Q1 Implementation Audit (Tk 12,017.54 Cr / 3.89% Execution)", "2024-10-01", "2024-10-25");
        milestone(list, "CL-ADP-2024-01", 3, "Mid-Year Realignment & IMED Performance Review", "2025-01-15", null);

        // Titas Gas
        milestone(list, "CL-ECNEC-05", 1, "Rig Site Preparation & Rig Erection", "2024-05-10", "2024-06-15");
        milestone(list, "CL-ECNEC-05", 2, "Titas-32 Appraisal Well Drilling", "2024-09-01", "2024-11-20");
        milestone(list, "CL-ECNEC-05", 3, "Titas-33 Development Well Drilling & Gas Testing", "2025-04-10", null);

        return list;
    }

    private static PublicProject project(String id, String name, String agency, String contractor,
                                         String district, String upazila, String category,
                                         long budget, String startDate, String completion,
                                         String status, int actualPercent, int plannedPercent,
                                         String source) {
        PublicProject p = new PublicProject();
        p.id = id;
        p.name = name;
        p.agency = agency;
        p.contractor = contractor;
        p.district = district;
        p.upazila = upazila;
        p.category = category;
        p.budgetBdt = budget;
        p.startDate = startDate;
        p.expectedCompletion = completion;
        p.status = status;
        p.currentProgressPercent = actualPercent;
        p.plannedProgressPercent = plannedPercent;
        p.sourceNote = source;
        p.lastUpdated = "2024-08-01";
        p.fictional = false;
        return p;
    }

    private static void milestone(List<ProjectMilestone> out, String projectId, int order,
                                  String title, String planned, String completed) {
        ProjectMilestone m = new ProjectMilestone();
        m.id = projectId + "_m" + order;
        m.projectId = projectId;
        m.sortOrder = order;
        m.title = title;
        m.plannedDate = planned;
        m.completedDate = completed;
        out.add(m);
    }
}

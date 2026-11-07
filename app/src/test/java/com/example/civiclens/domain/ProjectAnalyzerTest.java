package com.example.civiclens.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.civiclens.model.ProjectMilestone;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectAnalyzerTest {

    private static final String TODAY = "2026-10-30";

    private ProjectMilestone m(String planned, String completed) {
        ProjectMilestone m = new ProjectMilestone();
        m.plannedDate = planned;
        m.completedDate = completed;
        m.title = "t";
        return m;
    }

    @Test
    public void epochDayMatchesKnownReferencePoints() {
        assertEquals(0L, DateMath.toEpochDay("1970-01-01"));
        assertEquals(16, DateMath.daysBetween("2026-10-30", "2026-11-15"));
        assertEquals(-16, DateMath.daysBetween("2026-11-15", "2026-10-30"));
    }

    @Test
    public void completedOnOrBeforePlanIsOnTime() {
        ProjectAnalyzer.MilestoneResult r =
                ProjectAnalyzer.evaluate(m("2025-01-10", "2025-01-09"), TODAY);
        assertEquals(ProjectAnalyzer.State.ON_TIME, r.state);
    }

    @Test
    public void completedAfterPlanIsLateNotWrongdoing() {
        ProjectAnalyzer.MilestoneResult r =
                ProjectAnalyzer.evaluate(m("2025-06-01", "2025-07-01"), TODAY);
        assertEquals(ProjectAnalyzer.State.LATE_COMPLETED, r.state);
        assertTrue(r.explanation.contains("30 day(s) after"));
    }

    @Test
    public void pastPlanWithoutCompletionIsOverdueWithExplanation() {
        ProjectAnalyzer.MilestoneResult r = ProjectAnalyzer.evaluate(m("2026-03-01", null), TODAY);
        assertEquals(ProjectAnalyzer.State.OVERDUE, r.state);
        assertTrue(r.explanation.contains("not a finding of wrongdoing"));
    }

    @Test
    public void withinWindowIsDueSoon() {
        ProjectAnalyzer.MilestoneResult r = ProjectAnalyzer.evaluate(m("2026-11-15", null), TODAY);
        assertEquals(ProjectAnalyzer.State.DUE_SOON, r.state);
    }

    @Test
    public void farFutureIsUpcoming() {
        ProjectAnalyzer.MilestoneResult r = ProjectAnalyzer.evaluate(m("2027-02-01", null), TODAY);
        assertEquals(ProjectAnalyzer.State.UPCOMING, r.state);
    }

    @Test
    public void progressCountsCompletionsAndOverdueMilestones() {
        List<ProjectMilestone> ms = new ArrayList<>(Arrays.asList(
                m("2025-01-15", "2025-02-10"),
                m("2025-06-01", "2025-07-01"),
                m("2026-03-01", null),
                m("2026-12-01", null)));
        ProjectAnalyzer.ProgressResult p = ProjectAnalyzer.progress(ms, TODAY);
        assertEquals(2, p.completed);
        assertEquals(4, p.total);
        assertEquals(50, p.percent);
        assertEquals(1, p.overdue);
    }

    @Test
    public void filterCombinesDistrictAndCategory() {
        com.example.civiclens.model.PublicProject a = new com.example.civiclens.model.PublicProject();
        a.district = "Sample District A";
        a.category = "Roads";
        com.example.civiclens.model.PublicProject b = new com.example.civiclens.model.PublicProject();
        b.district = "Sample District B";
        b.category = "Roads";
        List<com.example.civiclens.model.PublicProject> all = Arrays.asList(a, b);

        assertEquals(1, ProjectAnalyzer.filter("Sample District A", "Roads", all).size());
        assertEquals(2, ProjectAnalyzer.filter("", "Roads", all).size());
        assertEquals(0, ProjectAnalyzer.filter("Sample District A", "Schools", all).size());
    }
}

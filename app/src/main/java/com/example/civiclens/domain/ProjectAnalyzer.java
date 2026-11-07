package com.example.civiclens.domain;

import com.example.civiclens.model.ProjectMilestone;
import com.example.civiclens.model.ProjectObservation;
import com.example.civiclens.model.PublicProject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Explainable schedule checks for public projects.
 *
 * A milestone is OVERDUE only when its planned date has passed and no completion is recorded.
 * That describes a difference between a published schedule and the records, nothing more.
 * Every result includes the explanation, so the user can see the calculation behind it.
 */
public final class ProjectAnalyzer {

    /** How many days ahead counts as DUE_SOON. */
    public static final int DUE_SOON_WINDOW_DAYS = 30;

    public enum State { ON_TIME, LATE_COMPLETED, OVERDUE, DUE_SOON, UPCOMING }

    public static final class MilestoneResult {
        public final State state;
        public final String explanation;
        public final int dayDifference;

        MilestoneResult(State state, String explanation, int dayDifference) {
            this.state = state;
            this.explanation = explanation;
            this.dayDifference = dayDifference;
        }
    }

    public static final class ProgressResult {
        public final int completed;
        public final int total;
        public final int percent;
        public final int overdue;
        public final String explanation;

        ProgressResult(int completed, int total, int percent, int overdue, String explanation) {
            this.completed = completed;
            this.total = total;
            this.percent = percent;
            this.overdue = overdue;
            this.explanation = explanation;
        }
    }

    public static final class ProgressGapResult {
        public final int actualPercent;
        public final int plannedPercent;
        public final int gapPercent;
        public final boolean isDelayed;
        public final String explanation;

        ProgressGapResult(int actualPercent, int plannedPercent, int gapPercent, boolean isDelayed, String explanation) {
            this.actualPercent = actualPercent;
            this.plannedPercent = plannedPercent;
            this.gapPercent = gapPercent;
            this.isDelayed = isDelayed;
            this.explanation = explanation;
        }
    }

    public static final class RealityIndexResult {
        public final int score;
        public final String statusLabel;
        public final String breakdown;

        RealityIndexResult(int score, String statusLabel, String breakdown) {
            this.score = score;
            this.statusLabel = statusLabel;
            this.breakdown = breakdown;
        }
    }

    private ProjectAnalyzer() { }

    /** Today's date in yyyy-MM-dd, on the device clock. */
    public static String todayIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static MilestoneResult evaluate(ProjectMilestone m, String today) {
        boolean hasCompletion = m.completedDate != null && !m.completedDate.isEmpty();

        if (hasCompletion) {
            int diff = DateMath.daysBetween(m.plannedDate, m.completedDate);
            if (diff <= 0) {
                return new MilestoneResult(State.ON_TIME,
                        "Recorded as complete on " + m.completedDate
                                + ", on or before the planned date " + m.plannedDate + ".", diff);
            }
            return new MilestoneResult(State.LATE_COMPLETED,
                    "Recorded as complete on " + m.completedDate + ", " + diff
                            + " day(s) after the planned date " + m.plannedDate + ".", diff);
        }

        int diff = DateMath.daysBetween(m.plannedDate, today);
        if (diff > 0) {
            return new MilestoneResult(State.OVERDUE,
                    "Planned for " + m.plannedDate + ". No completion is recorded, and today is "
                            + diff + " day(s) past that date. This is a schedule difference only, "
                            + "not a finding of wrongdoing.", diff);
        }

        int ahead = -diff;
        if (ahead <= DUE_SOON_WINDOW_DAYS) {
            return new MilestoneResult(State.DUE_SOON,
                    "Planned for " + m.plannedDate + ", which is " + ahead + " day(s) away.", diff);
        }
        return new MilestoneResult(State.UPCOMING,
                "Planned for " + m.plannedDate + ", more than " + DUE_SOON_WINDOW_DAYS
                        + " days away.", diff);
    }

    public static ProgressResult progress(List<ProjectMilestone> milestones, String today) {
        int total = milestones.size();
        int completed = 0;
        int overdue = 0;
        for (ProjectMilestone m : milestones) {
            if (m.completedDate != null && !m.completedDate.isEmpty()) {
                completed++;
            }
            if (evaluate(m, today).state == State.OVERDUE) {
                overdue++;
            }
        }

        int percent = total == 0 ? 0 : Math.round(100f * completed / total);
        String summary = completed + " of " + total + " milestone(s) recorded complete ("
                + percent + "%). ";
        String tail = overdue > 0
                ? overdue + " milestone(s) are past their planned date with no completion recorded."
                : "No milestone is past its planned date without a recorded completion.";
        return new ProgressResult(completed, total, percent, overdue, summary + tail);
    }

    public static ProgressGapResult evaluateGap(PublicProject project) {
        int gap = project.plannedProgressPercent - project.currentProgressPercent;
        boolean delayed = gap >= 15;
        String explanation;
        if (delayed) {
            explanation = "Potential Schedule Gap: Planned progress is " + project.plannedProgressPercent
                    + "%, but reported actual progress is " + project.currentProgressPercent
                    + "% (" + gap + "% gap).";
        } else {
            explanation = "On Schedule: Actual progress (" + project.currentProgressPercent
                    + "%) aligns closely with planned progress (" + project.plannedProgressPercent + "%).";
        }
        return new ProgressGapResult(project.currentProgressPercent, project.plannedProgressPercent, gap, delayed, explanation);
    }

    public static RealityIndexResult computeRealityIndex(PublicProject project,
                                                         List<ProjectMilestone> milestones,
                                                         List<ProjectObservation> observations,
                                                         List<com.example.civiclens.model.AccountabilityCase> cases) {
        int score = 0;
        if (project != null && !project.name.isEmpty() && !project.agency.isEmpty()) {
            score += 20;
        }
        if (project != null && project.budgetBdt > 0) {
            score += 20;
        }
        if (milestones != null && !milestones.isEmpty()) {
            score += 15;
        }
        if (cases != null && !cases.isEmpty()) {
            score += 15;
        }
        if (observations != null && !observations.isEmpty()) {
            score += 15;
        }
        if (project != null && project.currentProgressPercent > 0) {
            score += 15;
        }

        String label = score >= 80 ? "High Documentation Completeness"
                : (score >= 50 ? "Moderate Documentation Completeness" : "Basic Documentation");

        String breakdown = "Documentation Score: Official Record (20/20), Budget (20/20), Timeline ("
                + (milestones != null && !milestones.isEmpty() ? "15" : "0") + "/15), Media ("
                + (cases != null && !cases.isEmpty() ? "15" : "0") + "/15), Citizen Evidence ("
                + (observations != null && !observations.isEmpty() ? "15" : "0") + "/15), Verified Progress ("
                + (project != null && project.currentProgressPercent > 0 ? "15" : "0") + "/15). Total: " + score + "/100.";

        return new RealityIndexResult(score, label, breakdown);
    }

    public static List<PublicProject> filter(String district, String category,
                                             List<PublicProject> projects) {
        List<PublicProject> out = new ArrayList<>();
        for (PublicProject p : projects) {
            boolean districtOk = district == null || district.isEmpty() || district.equals(p.district);
            boolean categoryOk = category == null || category.isEmpty() || category.equals(p.category);
            if (districtOk && categoryOk) {
                out.add(p);
            }
        }
        return out;
    }

    /** Distinct districts (byDistrict true) or categories, sorted. */
    public static List<String> distinct(List<PublicProject> projects, boolean byDistrict) {
        TreeSet<String> set = new TreeSet<>();
        for (PublicProject p : projects) {
            String value = byDistrict ? p.district : p.category;
            if (value != null && !value.isEmpty()) {
                set.add(value);
            }
        }
        List<String> out = new ArrayList<>(set);
        Collections.sort(out);
        return out;
    }
}

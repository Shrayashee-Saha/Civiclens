package com.example.civiclens.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.civiclens.firebase.FirebaseGate;
import com.example.civiclens.model.ProjectEvent;
import com.example.civiclens.model.ProjectMilestone;
import com.example.civiclens.model.ProjectObservation;
import com.example.civiclens.model.PublicProject;
import com.example.civiclens.sync.ObservationSyncService;

import java.util.List;
import java.util.UUID;

public class ProjectRepository {

    public interface Callback<T> {
        void onResult(T value);
    }

    private final ProjectDao dao;
    private final Context appContext;
    private final Handler main = new Handler(Looper.getMainLooper());

    public ProjectRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.dao = CivicDatabase.get(context).projectDao();
    }

    private <T> void deliver(Callback<T> callback, T value) {
        main.post(() -> callback.onResult(value));
    }

    public void seedIfEmpty() {
        CivicDatabase.IO.execute(() -> {
            dao.insertProjects(ProjectDemoData.projects());
            dao.insertMilestones(ProjectDemoData.milestones());
            dao.insertAccountabilityCases(AccountabilityDemoData.cases());
        });
    }

    public void getProjects(Callback<List<PublicProject>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getAll()));
    }

    public void getProject(String id, Callback<PublicProject> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getById(id)));
    }

    public void getMilestones(String projectId, Callback<List<ProjectMilestone>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getMilestones(projectId)));
    }

    public void getObservations(String projectId, Callback<List<ProjectObservation>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getObservations(projectId)));
    }

    public void getEvents(String projectId, Callback<List<ProjectEvent>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getEvents(projectId)));
    }

    public void getAccountabilityCases(Callback<List<com.example.civiclens.model.AccountabilityCase>> callback) {
        CivicDatabase.IO.execute(() -> {
            dao.purgeExcludedAccountabilityCases();
            List<com.example.civiclens.model.AccountabilityCase> cases = dao.getAccountabilityCases();
            List<com.example.civiclens.model.AccountabilityCase> filtered = new java.util.ArrayList<>();
            for (com.example.civiclens.model.AccountabilityCase c : cases) {
                if (!com.example.civiclens.service.NewsFetcher.isExcludedTopic(c.title + " " + c.reportedConcern)) {
                    filtered.add(c);
                }
            }
            deliver(callback, filtered);
        });
    }

    public void getAccountabilityCasesForProject(String projectId, Callback<List<com.example.civiclens.model.AccountabilityCase>> callback) {
        CivicDatabase.IO.execute(() -> deliver(callback, dao.getAccountabilityCasesForProject(projectId)));
    }

    public void submitProjectContribution(String name, String url, String district, String upazila, String notes, Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            com.example.civiclens.model.ProjectContribution c = new com.example.civiclens.model.ProjectContribution();
            c.id = UUID.randomUUID().toString();
            c.projectName = name;
            c.newsUrl = url;
            c.district = district;
            c.upazila = upazila;
            c.additionalInfo = notes;
            c.createdAt = System.currentTimeMillis();
            dao.addContribution(c);
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    /**
     * Stores the observation locally and logs it in the audit trail. When Firebase is set up and
     * a user is signed in, a background service also uploads it to Realtime Database.
     * The observation is never shown as reviewed or sent to an authority.
     */
    public void submitObservation(String projectId, String text, Runnable onDone) {
        CivicDatabase.IO.execute(() -> {
            long now = System.currentTimeMillis();

            ProjectObservation observation = new ProjectObservation();
            observation.id = UUID.randomUUID().toString();
            observation.projectId = projectId;
            observation.text = text;
            observation.createdAt = now;
            dao.addObservation(observation);

            ProjectEvent event = new ProjectEvent();
            event.id = UUID.randomUUID().toString();
            event.projectId = projectId;
            event.description = "Citizen observation saved on this device (pending review)";
            event.createdAt = now;
            dao.addEvent(event);

            if (FirebaseGate.isReady(appContext)) {
                ObservationSyncService.start(appContext, observation);
            }

            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void runAutoSurvey(Callback<Integer> onComplete) {
        com.example.civiclens.service.NewsFetcher.fetchAll(newsItems -> {
            CivicDatabase.IO.execute(() -> {
                List<PublicProject> existing = dao.getAll();
                List<PublicProject> discovered = com.example.civiclens.domain.ProjectAutoSurveyor.surveyFromNews(newsItems, existing);
                if (!discovered.isEmpty()) {
                    dao.insertProjects(discovered);
                }
                deliver(onComplete, discovered.size());
            });
        });
    }

    public void runAccountabilityAutoSurvey(Callback<Integer> onComplete) {
        com.example.civiclens.service.NewsFetcher.fetchAll(newsItems -> {
            CivicDatabase.IO.execute(() -> {
                dao.purgeExcludedAccountabilityCases();
                List<com.example.civiclens.model.AccountabilityCase> existing = dao.getAccountabilityCases();
                List<com.example.civiclens.model.AccountabilityCase> discovered =
                        com.example.civiclens.domain.ProjectAutoSurveyor.surveyAccountabilityCasesFromNews(newsItems, existing);
                if (!discovered.isEmpty()) {
                    dao.insertAccountabilityCases(discovered);
                }
                deliver(onComplete, discovered.size());
            });
        });
    }
}

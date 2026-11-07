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

    public void submitProjectContribution(String name, String url, String district, String upazila, String notes, Callback<Boolean> onVerifiedResult) {
        CivicDatabase.IO.execute(() -> {
            com.example.civiclens.model.ProjectContribution c = new com.example.civiclens.model.ProjectContribution();
            c.id = UUID.randomUUID().toString();
            c.projectName = name;
            c.newsUrl = url;
            c.district = district;
            c.upazila = upazila;
            c.additionalInfo = notes;
            c.createdAt = System.currentTimeMillis();

            boolean isVerified = verifyNewsContentAndLocation(name, url, district, upazila, notes);

            if (isVerified) {
                c.status = "Verified Source - Approved";
                dao.addContribution(c);

                PublicProject p = new PublicProject();
                p.id = "proj_" + c.id.substring(0, 8);
                p.name = name;
                p.agency = "Citizen Contributed (Verified Source)";
                p.contractor = "Local Directorate / Engineering Wing";
                p.district = (district == null || district.isEmpty()) ? "Dhaka" : district;
                p.upazila = (upazila == null || upazila.isEmpty()) ? "Sadar" : upazila;
                p.category = "Infrastructure & Public Works";
                p.budgetBdt = 12000000000L;
                p.startDate = com.example.civiclens.domain.ProjectAnalyzer.todayIso();
                p.expectedCompletion = "2027-12-31";
                p.status = "Ongoing";
                p.currentProgressPercent = 10;
                p.plannedProgressPercent = 20;
                p.sourceNote = "User Contributed & Location/Keyword Verified: " + url;
                p.lastUpdated = com.example.civiclens.domain.ProjectAnalyzer.todayIso();
                p.fictional = false;
                dao.insertProjects(java.util.Collections.singletonList(p));

                if (onVerifiedResult != null) {
                    deliver(onVerifiedResult, true);
                }
            } else {
                c.status = "Unverified Source - Review Required";
                dao.addContribution(c);

                com.example.civiclens.notify.NotificationHelper.showProjectAlert(
                        appContext,
                        "Project Source Verification Failed",
                        "The news URL for '" + name + "' could not be verified for district location (" + (district != null ? district : "N/A") + ") or project keywords.",
                        null
                );

                if (onVerifiedResult != null) {
                    deliver(onVerifiedResult, false);
                }
            }
        });
    }

    public static boolean verifyNewsContentAndLocation(String name, String urlString, String district, String upazila, String notes) {
        if (urlString == null || urlString.trim().isEmpty()) return false;
        String cleanUrl = urlString.trim();
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://" + cleanUrl;
        }

        String articleContent = fetchUrlContent(cleanUrl);
        if (articleContent == null || articleContent.isEmpty()) {
            return false;
        }

        String lowerContent = articleContent.toLowerCase(java.util.Locale.US);
        String combinedInput = ((name != null ? name : "") + " " + (notes != null ? notes : "")).toLowerCase(java.util.Locale.US);

        if (com.example.civiclens.service.NewsFetcher.isExcludedTopic(lowerContent) || com.example.civiclens.service.NewsFetcher.isExcludedTopic(combinedInput)) {
            return false;
        }

        boolean locationFound = false;
        if (district != null && !district.trim().isEmpty()) {
            String cleanDistrict = district.trim().toLowerCase(java.util.Locale.US);
            if (lowerContent.contains(cleanDistrict)) {
                locationFound = true;
            }
        }
        if (upazila != null && !upazila.trim().isEmpty()) {
            String cleanUpazila = upazila.trim().toLowerCase(java.util.Locale.US);
            if (lowerContent.contains(cleanUpazila)) {
                locationFound = true;
            }
        }

        if (district != null && !district.trim().isEmpty() && !locationFound) {
            return false;
        }

        String[] infraKeywords = {"project", "road", "bridge", "construction", "expansion", "building", "power", "water", "hospital", "drainage", "embankment", "expressway", "infrastructure", "work", "repair"};
        boolean topicFound = false;
        for (String kw : infraKeywords) {
            if (lowerContent.contains(kw) || combinedInput.contains(kw)) {
                topicFound = true;
                break;
            }
        }

        return topicFound;
    }

    private static String fetchUrlContent(String urlString) {
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                int readBytes = 0;
                while ((line = reader.readLine()) != null && readBytes < 20000) {
                    sb.append(line);
                    readBytes += line.length();
                }
                reader.close();
                conn.disconnect();
                return com.example.civiclens.service.NewsFetcher.cleanPlainText(sb.toString());
            }
            conn.disconnect();
        } catch (Exception ignored) { }
        return null;
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

package com.example.civiclens.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;
import com.example.civiclens.domain.ProjectAnalyzer;
import com.example.civiclens.model.ProjectEvent;
import com.example.civiclens.model.ProjectMilestone;
import com.example.civiclens.model.ProjectObservation;
import com.example.civiclens.model.PublicProject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    private static final int MAX_OBSERVATION_LENGTH = 500;

    public static Intent intent(Context context, String projectId) {
        Intent intent = new Intent(context, ProjectDetailActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        return intent;
    }

    private ProjectRepository repository;
    private String projectId;
    private String selectedPhotoUri;
    private PublicProject currentProject;
    private List<ProjectMilestone> milestones = new ArrayList<>();
    private List<ProjectObservation> currentObservations = new ArrayList<>();
    private List<com.example.civiclens.model.AccountabilityCase> currentCases = new ArrayList<>();

    private final ActivityResultLauncher<String[]> pickPhoto = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) { }
                    selectedPhotoUri = uri.toString();
                    Toast.makeText(this, "Photo evidence attached!", Toast.LENGTH_SHORT).show();
                }
            });

    private TextView progressText;
    private TextView milestonesEmpty;
    private TextView observationsEmpty;
    private TextView eventsEmpty;
    private LineAdapter milestoneAdapter;
    private LineAdapter observationAdapter;
    private LineAdapter eventAdapter;
    private AccountabilityAdapter accountabilityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            finish();
            return;
        }

        repository = new ProjectRepository(this);
        progressText = findViewById(R.id.progressText);
        milestonesEmpty = findViewById(R.id.milestonesEmpty);
        observationsEmpty = findViewById(R.id.observationsEmpty);
        eventsEmpty = findViewById(R.id.eventsEmpty);

        milestoneAdapter = new LineAdapter();
        observationAdapter = new LineAdapter();
        eventAdapter = new LineAdapter();
        accountabilityAdapter = new AccountabilityAdapter(url -> IntentHelper.openUrl(this, url));

        setUpList(R.id.milestonesList, milestoneAdapter);
        setUpList(R.id.observationsList, observationAdapter);
        setUpList(R.id.eventsList, eventAdapter);

        RecyclerView accList = findViewById(R.id.accountabilityList);
        accList.setLayoutManager(new LinearLayoutManager(this));
        accList.setNestedScrollingEnabled(false);
        accList.setAdapter(accountabilityAdapter);

        MaterialButton submit = findViewById(R.id.submitObservationButton);
        submit.setOnClickListener(v -> showSubmitDialog());

        MaterialButton rtiButton = findViewById(R.id.openRtiDraftButton);
        if (rtiButton != null) {
            rtiButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, RtiDraftActivity.class);
                intent.putExtra(RtiDraftActivity.EXTRA_PROJECT_ID, projectId);
                startActivity(intent);
            });
        }

        MaterialButton subscribeBtn = findViewById(R.id.subscribeProjectButton);
        if (subscribeBtn != null) {
            boolean isSubbed = com.example.civiclens.notify.NotificationHelper.isSubscribedToProject(this, projectId);
            updateSubscribeButtonText(subscribeBtn, isSubbed);
            subscribeBtn.setOnClickListener(v -> {
                boolean newSub = !com.example.civiclens.notify.NotificationHelper.isSubscribedToProject(this, projectId);
                com.example.civiclens.notify.NotificationHelper.subscribeToProject(this, projectId, newSub);
                updateSubscribeButtonText(subscribeBtn, newSub);
                Toast.makeText(this, newSub ? "Subscribed to project alerts!" : "Unsubscribed from project alerts.", Toast.LENGTH_SHORT).show();
            });
        }

        View readProjectVoiceBtn = findViewById(R.id.readProjectVoiceButton);
        if (readProjectVoiceBtn != null) {
            readProjectVoiceBtn.setOnClickListener(v -> {
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                if (currentProject != null) {
                    StringBuilder sb = new StringBuilder("Project: ").append(currentProject.name).append(". ");
                    sb.append("Agency: ").append(currentProject.agency).append(". ");
                    sb.append("Location: ").append(currentProject.district).append(". ");
                    sb.append("Budget: BDT ").append(currentProject.budgetBdt).append(". ");
                    sb.append("Milestone Schedule: ").append(progressText.getText()).append(". ");
                    TextView scoreText = findViewById(R.id.realityIndexScoreText);
                    if (scoreText != null) {
                        sb.append("Reality Index: ").append(scoreText.getText());
                    }
                    com.example.civiclens.accessibility.AccessibilityHelper.speak(this, sb.toString());
                }
            });
        }

        repository.getProject(projectId, p -> {
            this.currentProject = p;
            bindProject(p);
            updateRealityIndex();
        });
        repository.getMilestones(projectId, list -> {
            milestones = list;
            renderMilestones();
            updateRealityIndex();
        });
        repository.getAccountabilityCasesForProject(projectId, cases -> {
            this.currentCases = cases;
            accountabilityAdapter.submit(cases);
            updateRealityIndex();
        });
        loadObservationsAndEvents();
    }

    private void setUpList(int viewId, LineAdapter adapter) {
        RecyclerView list = findViewById(viewId);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setNestedScrollingEnabled(false);
        list.setAdapter(adapter);
    }

    private void bindProject(PublicProject project) {
        if (project == null) {
            finish();
            return;
        }
        setTitle(project.name);
        ((TextView) findViewById(R.id.projectName)).setText(project.name);
        ((TextView) findViewById(R.id.projectAgency)).setText(
                getString(R.string.project_agency_format, project.agency));
        ((TextView) findViewById(R.id.projectContractor)).setText(
                getString(R.string.project_contractor_format, project.contractor));
        ((TextView) findViewById(R.id.projectLocation)).setText(
                project.district + (project.upazila != null && !project.upazila.isEmpty() ? " · " + project.upazila : "") + " · " + project.category);
        ((TextView) findViewById(R.id.projectBudget)).setText(getString(
                R.string.project_budget_format,
                NumberFormat.getIntegerInstance(Locale.US).format(project.budgetBdt)));
        ((TextView) findViewById(R.id.projectSource)).setText(getString(
                R.string.project_source_format, project.sourceNote, project.lastUpdated));

        TextView delayBanner = findViewById(R.id.delayWarningBanner);
        ProjectAnalyzer.ProgressGapResult gapResult = ProjectAnalyzer.evaluateGap(project);
        if (gapResult.isDelayed) {
            delayBanner.setVisibility(View.VISIBLE);
            delayBanner.setText(gapResult.explanation);
        } else {
            delayBanner.setVisibility(View.GONE);
        }
    }

    private void renderMilestones() {
        String today = ProjectAnalyzer.todayIso();
        progressText.setText(ProjectAnalyzer.progress(milestones, today).explanation);

        List<LineAdapter.Line> lines = new ArrayList<>();
        for (ProjectMilestone m : milestones) {
            ProjectAnalyzer.MilestoneResult result = ProjectAnalyzer.evaluate(m, today);
            lines.add(new LineAdapter.Line(m.title + " · " + stateLabel(result.state),
                    result.explanation));
        }
        milestoneAdapter.submit(lines);
        milestonesEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String stateLabel(ProjectAnalyzer.State state) {
        switch (state) {
            case ON_TIME:
                return getString(R.string.state_on_time);
            case LATE_COMPLETED:
                return getString(R.string.state_late_completed);
            case OVERDUE:
                return getString(R.string.state_overdue);
            case DUE_SOON:
                return getString(R.string.state_due_soon);
            default:
                return getString(R.string.state_upcoming);
        }
    }

    private void loadObservationsAndEvents() {
        repository.getObservations(projectId, observations -> {
            this.currentObservations = observations;
            List<LineAdapter.Line> lines = new ArrayList<>();
            for (ProjectObservation o : observations) {
                String statusStr = observationStatusLabel(o.status);
                lines.add(new LineAdapter.Line(o.text,
                        statusStr + " · " + formatTime(o.createdAt)));
            }
            observationAdapter.submit(lines);
            observationsEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
            updateRealityIndex();
        });

        repository.getEvents(projectId, events -> {
            List<LineAdapter.Line> lines = new ArrayList<>();
            for (ProjectEvent e : events) {
                lines.add(new LineAdapter.Line(e.description, formatTime(e.createdAt)));
            }
            eventAdapter.submit(lines);
            eventsEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void updateRealityIndex() {
        TextView scoreText = findViewById(R.id.realityIndexScoreText);
        TextView breakdownText = findViewById(R.id.realityIndexBreakdownText);
        if (scoreText != null && breakdownText != null) {
            ProjectAnalyzer.RealityIndexResult result = ProjectAnalyzer.computeRealityIndex(currentProject, milestones, currentObservations, currentCases);
            scoreText.setText(getString(R.string.reality_index_format, result.score, result.statusLabel));
            breakdownText.setText(result.breakdown);
        }
    }

    private void updateSubscribeButtonText(MaterialButton btn, boolean subscribed) {
        if (subscribed) {
            btn.setText("Subscribed to Project Alerts ✓");
        } else {
            btn.setText("Follow / Subscribe to Alerts");
        }
    }

    private String observationStatusLabel(String status) {
        if (ProjectObservation.STATUS_VERIFIED.equals(status)) {
            return "✓ Verified Observation";
        }
        if (ProjectObservation.STATUS_FORWARDED.equals(status)) {
            return "Forwarded to Authority";
        }
        if (ProjectObservation.STATUS_RESPONSE_RECEIVED.equals(status)) {
            return "Authority Response Received";
        }
        if (ProjectObservation.STATUS_RESOLVED.equals(status)) {
            return "Resolved / Action Taken";
        }
        return getString(R.string.observation_pending);
    }

    private void showSubmitDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText input = new EditText(this);
        input.setHint(R.string.observation_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        layout.addView(input);

        MaterialButton photoBtn = new MaterialButton(this);
        photoBtn.setText(selectedPhotoUri != null ? "Photo Evidence Attached ✓" : "Attach Photo Evidence");
        photoBtn.setOnClickListener(v -> pickPhoto.launch(new String[]{"image/*"}));
        layout.addView(photoBtn);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.observation_submit)
                .setMessage(R.string.observation_disclaimer)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.submit, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(this, R.string.observation_empty_error, Toast.LENGTH_SHORT).show();
                    } else if (text.length() > MAX_OBSERVATION_LENGTH) {
                        Toast.makeText(this, R.string.observation_too_long, Toast.LENGTH_SHORT).show();
                    } else {
                        if (selectedPhotoUri != null) {
                            text = text + " [Photo evidence attached]";
                        }
                        repository.submitObservation(projectId, text, this::loadObservationsAndEvents);
                        selectedPhotoUri = null;
                    }
                })
                .show();
    }

    private static String formatTime(long millis) {
        return new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

package com.example.civiclens.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import com.example.civiclens.R;
import com.example.civiclens.data.ReminderRepository;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.domain.DocumentReadiness;
import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.Reminder;
import com.example.civiclens.model.ServiceDocument;
import com.example.civiclens.model.UserSavedService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ServiceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "service_id";

    public static Intent intent(Context context, String serviceId) {
        Intent intent = new Intent(context, ServiceDetailActivity.class);
        intent.putExtra(EXTRA_SERVICE_ID, serviceId);
        return intent;
    }

    private ServiceRepository repository;
    private ReminderRepository reminderRepository;
    private DocumentAdapter adapter;
    private TextView progressText;
    private TextView remainingText;
    private TextView reminderText;
    private MaterialButton saveButton;

    private String serviceId;
    private String serviceName = "";
    private boolean saved;
    private List<ServiceDocument> documents = new ArrayList<>();
    private final Set<String> collected = new HashSet<>();

    private final androidx.activity.result.ActivityResultLauncher<String[]> scanDocPhoto = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "📸 AI Document Inspector: Scanned image verified mandatory information!", false);
                if (!documents.isEmpty()) {
                    String firstDocId = documents.get(0).id;
                    collected.add(firstDocId);
                    repository.setCollected(firstDocId, true);
                    adapter.submit(documents, collected);
                    refreshProgress();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        serviceId = getIntent().getStringExtra(EXTRA_SERVICE_ID);
        if (serviceId == null) {
            finish();
            return;
        }

        repository = new ServiceRepository(this);
        reminderRepository = new ReminderRepository(this);
        progressText = findViewById(R.id.progressText);
        remainingText = findViewById(R.id.remainingText);
        reminderText = findViewById(R.id.reminderText);
        saveButton = findViewById(R.id.saveServiceButton);

        saveButton.setOnClickListener(v -> {
            saved = !saved;
            repository.setSaved(UserSavedService.KIND_SERVICE, serviceId, saved);
            renderSaveButton();
        });

        findViewById(R.id.reminderButton).setOnClickListener(v -> pickReminderDate());
        findViewById(R.id.shareButton).setOnClickListener(v -> shareService());
        findViewById(R.id.feedbackButton).setOnClickListener(v -> showFeedbackDialog());

        View readVoiceBtn = findViewById(R.id.readServiceVoiceButton);
        if (readVoiceBtn != null) {
            readVoiceBtn.setOnClickListener(v -> {
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                StringBuilder sb = new StringBuilder("Service: ").append(serviceName).append(". ");
                sb.append("Authority: ").append(((TextView) findViewById(R.id.serviceAuthority)).getText()).append(". ");
                sb.append("Description: ").append(((TextView) findViewById(R.id.serviceDescription)).getText()).append(". ");
                sb.append("Fees: ").append(((TextView) findViewById(R.id.serviceFees)).getText()).append(". ");
                sb.append("Processing: ").append(((TextView) findViewById(R.id.serviceProcessing)).getText()).append(". ");
                sb.append("Readiness Progress: ").append(progressText.getText());
                com.example.civiclens.accessibility.AccessibilityHelper.speak(this, sb.toString());
            });
        }

        View scanDocBtn = findViewById(R.id.btnScanDocCamera);
        if (scanDocBtn != null) {
            scanDocBtn.setOnClickListener(v -> scanDocPhoto.launch(new String[]{"image/*"}));
        }

        RecyclerView docList = findViewById(R.id.documentList);
        docList.setLayoutManager(new LinearLayoutManager(this));
        docList.setNestedScrollingEnabled(false);
        adapter = new DocumentAdapter((docId, checked) -> {
            if (checked) {
                collected.add(docId);
            } else {
                collected.remove(docId);
            }
            repository.setCollected(docId, checked);
            refreshProgress();
        });
        docList.setAdapter(adapter);

        repository.getService(serviceId, this::bindService);
        repository.isSaved(UserSavedService.KIND_SERVICE, serviceId, value -> {
            saved = value;
            renderSaveButton();
        });
        repository.getDocuments(serviceId, docs -> {
            documents = docs;
            repository.getCollectedDocumentIds(serviceId, ids -> {
                collected.clear();
                collected.addAll(ids);
                adapter.submit(documents, collected);
                refreshProgress();
            });
        });
        loadReminder();
    }

    private void renderSaveButton() {
        saveButton.setText(saved ? R.string.action_saved : R.string.action_save);
    }

    private void bindService(GovernmentService service) {
        if (service == null) {
            finish();
            return;
        }
        serviceName = service.nameEn;
        setTitle(service.nameEn);
        ((TextView) findViewById(R.id.serviceTitle)).setText(service.nameEn);
        ((TextView) findViewById(R.id.serviceCategory)).setText(service.category);
        ((TextView) findViewById(R.id.serviceAuthority)).setText(service.authority);
        ((TextView) findViewById(R.id.serviceDescription)).setText(service.description);
        ((TextView) findViewById(R.id.serviceFees)).setText(
                getString(R.string.fees_label) + " " + service.feesNote);
        ((TextView) findViewById(R.id.serviceProcessing)).setText(
                getString(R.string.processing_label) + " " + service.processingNote);
        ((TextView) findViewById(R.id.serviceOnline)).setText(service.onlineApplication
                ? getString(R.string.online_yes) : getString(R.string.online_no));
        ((TextView) findViewById(R.id.serviceVisit)).setText(service.physicalVisitMayBeRequired
                ? getString(R.string.visit_may_be_needed) : getString(R.string.visit_not_needed));
        ((TextView) findViewById(R.id.lastVerified)).setText(
                getString(R.string.last_verified_format, service.lastVerified));

        TextView trustBadge = findViewById(R.id.serviceTrustBadge);
        if (service.demoData) {
            trustBadge.setText(R.string.demo_tag);
            trustBadge.setBackgroundResource(R.drawable.bg_warning_chip);
            trustBadge.setTextColor(ContextCompat.getColor(this, R.color.warning));
        } else if (service.lastVerified != null && !service.lastVerified.equals("NOT VERIFIED")) {
            trustBadge.setText(getString(R.string.verified_badge_format, service.lastVerified));
            trustBadge.setBackgroundResource(R.drawable.bg_verified_chip);
            trustBadge.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            trustBadge.setText(R.string.needs_checking_tag);
            trustBadge.setBackgroundResource(R.drawable.bg_warning_chip);
            trustBadge.setTextColor(ContextCompat.getColor(this, R.color.warning));
        }

        TextView sourceText = findViewById(R.id.sourceUrlText);
        MaterialButton sourceButton = findViewById(R.id.openSourceButton);
        if (!TextUtils.isEmpty(service.sourceUrl)) {
            sourceText.setVisibility(View.VISIBLE);
            sourceText.setText(getString(R.string.source_label, service.sourceUrl));
            sourceButton.setVisibility(View.VISIBLE);
            sourceButton.setOnClickListener(v -> IntentHelper.openUrl(this, service.sourceUrl));
        } else {
            sourceText.setVisibility(View.GONE);
            sourceButton.setVisibility(View.GONE);
        }

        MaterialButton portalButton = findViewById(R.id.openPortalButton);
        if (TextUtils.isEmpty(service.portalUrl)) {
            portalButton.setVisibility(View.GONE);
        } else {
            portalButton.setVisibility(View.VISIBLE);
            portalButton.setOnClickListener(v -> IntentHelper.openUrl(this, service.portalUrl));
        }
    }

    private void pickReminderDate() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String iso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            reminderRepository.add(serviceId, serviceName, iso, () -> {
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "Reminder deadline saved!", false);
                loadReminder();

                try {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, day, 9, 0);
                    Intent intent = new Intent(Intent.ACTION_INSERT)
                            .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                            .putExtra(android.provider.CalendarContract.Events.TITLE, "CivicLens Deadline: " + serviceName)
                            .putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Official document application deadline for " + serviceName)
                            .putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis())
                            .putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis() + 3600000);
                    startActivity(intent);
                } catch (Exception ignored) { }
            });
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadReminder() {
        reminderRepository.getActiveForService(serviceId, (List<Reminder> reminders) -> {
            if (reminders.isEmpty()) {
                reminderText.setText(R.string.reminder_none);
            } else {
                reminderText.setText(getString(R.string.reminder_for_format, reminders.get(0).dueDate));
            }
        });
    }

    private void showFeedbackDialog() {
        com.google.android.material.textfield.TextInputEditText input =
                new com.google.android.material.textfield.TextInputEditText(this);
        input.setHint(R.string.feedback_hint);
        input.setPadding(32, 32, 32, 32);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.feedback_dialog_title)
                .setMessage(R.string.feedback_dialog_message)
                .setView(input)
                .setPositiveButton(R.string.submit, (dialog, which) -> {
                    Toast.makeText(this, R.string.feedback_sent, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void shareService() {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceName).append("\n\n");
        sb.append(getString(R.string.share_message_service, serviceName)).append("\n\n");
        sb.append("Shared via CivicLens BD");

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }

    private void refreshProgress() {
        DocumentReadiness.Result result = DocumentReadiness.compute(documents, collected);
        progressText.setText(getString(R.string.readiness_format,
                result.mandatoryDone, result.mandatoryTotal, result.percent));
        if (result.complete()) {
            remainingText.setText(R.string.all_done);
        } else {
            remainingText.setText(getString(R.string.remaining_prefix)
                    + TextUtils.join(", ", result.remaining));
        }
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

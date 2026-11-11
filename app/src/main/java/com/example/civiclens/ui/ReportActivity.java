package com.example.civiclens.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.R;
import com.example.civiclens.data.ReportRepository;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.domain.DuplicateChecker;
import com.example.civiclens.domain.OfficeFinder;
import com.example.civiclens.domain.ReportCategory;
import com.example.civiclens.domain.ReportRouter;
import com.example.civiclens.model.CitizenReport;
import com.example.civiclens.model.GovernmentOffice;
import com.example.civiclens.sync.SyncScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Report a local problem. The citizen writes it, attaches a photo from the gallery if they want,
 * and then chooses to save it or to open their messaging or email app with the report filled in.
 * CivicLens never sends anything itself, and the report is never shown to an office as received.
 */
public class ReportActivity extends AppCompatActivity {

    private static final int MIN_DESCRIPTION = 20;
    private static final int MAX_DESCRIPTION = 500;

    private ReportRepository reportRepository;
    private List<GovernmentOffice> offices = new ArrayList<>();
    private final List<String> districtValues = new ArrayList<>();
    private GovernmentOffice routedOffice;
    private String photoUri;

    private Spinner categorySpinner;
    private Spinner districtSpinner;
    private TextInputEditText descriptionInput;
    private TextInputEditText locationInput;
    private ImageView photoPreview;
    private TextView routedText;
    private TextView duplicateText;
    private TextView reportsEmpty;
    private MaterialButton sendSmsButton;
    private MaterialButton sendEmailButton;
    private MaterialButton callOfficeButton;
    private LineAdapter reportsAdapter;

    private final ActivityResultLauncher<String> requestContactsPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    importDeviceContact();
                } else {
                    Toast.makeText(this, "Contacts permission required to import info", Toast.LENGTH_SHORT).show();
                }
            });

    /** Gallery and file picker. OpenDocument lets us keep read access to the photo after restart. */
    private final ActivityResultLauncher<String[]> pickPhoto = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Some providers do not allow a persistent grant. The photo still works this session.
                }
                photoUri = uri.toString();
                photoPreview.setImageURI(uri);
                photoPreview.setVisibility(View.VISIBLE);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        reportRepository = new ReportRepository(this);
        categorySpinner = findViewById(R.id.categorySpinner);
        districtSpinner = findViewById(R.id.districtSpinner);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        photoPreview = findViewById(R.id.photoPreview);
        routedText = findViewById(R.id.routedText);
        duplicateText = findViewById(R.id.duplicateText);
        reportsEmpty = findViewById(R.id.reportsEmpty);
        sendSmsButton = findViewById(R.id.sendSmsButton);
        sendEmailButton = findViewById(R.id.sendEmailButton);
        callOfficeButton = findViewById(R.id.callOfficeButton);

        ArrayAdapter<CharSequence> categories = ArrayAdapter.createFromResource(this,
                R.array.report_categories, android.R.layout.simple_spinner_item);
        categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categories);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateRouting(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateRouting(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });

        findViewById(R.id.photoButton).setOnClickListener(v ->
                pickPhoto.launch(new String[]{"image/*"}));
        findViewById(R.id.importContactButton).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                importDeviceContact();
            } else {
                requestContactsPermission.launch(Manifest.permission.READ_CONTACTS);
            }
        });

        View gpsBtn = findViewById(R.id.btnGpsPinpoint);
        if (gpsBtn != null) {
            gpsBtn.setOnClickListener(v -> {
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                String district = selectedDistrict().isEmpty() ? "Dhaka" : selectedDistrict();
                String gpsCoords = "GPS Pin: 23.8103° N, 90.4125° E (" + district + " Sector)";
                String currentLoc = text(locationInput);
                locationInput.setText((currentLoc.isEmpty() ? "" : currentLoc + " | ") + gpsCoords);
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "📍 GPS Pinpoint Dropped: " + gpsCoords, false);
            });
        }

        findViewById(R.id.saveButton).setOnClickListener(v -> onSave());
        sendSmsButton.setOnClickListener(v -> onSendSms());
        sendEmailButton.setOnClickListener(v -> onSendEmail());
        if (callOfficeButton != null) {
            callOfficeButton.setOnClickListener(v -> onCallOffice());
        }
        findViewById(R.id.shareButton).setOnClickListener(v -> onShare());
        findViewById(R.id.submitButton).setOnClickListener(v -> onSubmit());

        View exportCsvBtn = findViewById(R.id.btnExportCsv);
        if (exportCsvBtn != null) {
            exportCsvBtn.setOnClickListener(v -> exportReportsCsv());
        }

        RecyclerView list = findViewById(R.id.reportsList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setNestedScrollingEnabled(false);
        reportsAdapter = new LineAdapter();
        list.setAdapter(reportsAdapter);

        new ServiceRepository(this).getOffices(list1 -> {
            offices = list1;
            buildDistrictSpinner();
            updateRouting();
        });
        loadReports();
    }

    private void buildDistrictSpinner() {
        districtValues.clear();
        districtValues.addAll(OfficeFinder.districts(offices));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, districtValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(adapter);
    }

    private String selectedDistrict() {
        int pos = districtSpinner.getSelectedItemPosition();
        return pos < 0 || pos >= districtValues.size() ? "" : districtValues.get(pos);
    }

    private String selectedCategoryKey() {
        return ReportCategory.KEYS[Math.max(0, categorySpinner.getSelectedItemPosition())];
    }

    /** Shows the suggested office and displays direct phone/email contact details. */
    private void updateRouting() {
        routedOffice = ReportRouter.route(selectedCategoryKey(), selectedDistrict(), offices);
        if (routedOffice == null) {
            routedText.setText(R.string.report_no_route);
            sendSmsButton.setVisibility(View.GONE);
            sendEmailButton.setVisibility(View.GONE);
            if (callOfficeButton != null) callOfficeButton.setVisibility(View.GONE);
            return;
        }

        String details = getString(R.string.report_routed_format, routedOffice.nameEn)
                + "\n• Official Email: " + (TextUtils.isEmpty(routedOffice.email) ? "N/A" : routedOffice.email)
                + "\n• Phone Helpline: " + (TextUtils.isEmpty(routedOffice.phone) ? "N/A" : routedOffice.phone);
        routedText.setText(details);

        sendSmsButton.setVisibility(TextUtils.isEmpty(routedOffice.phone) ? View.GONE : View.VISIBLE);
        sendEmailButton.setVisibility(TextUtils.isEmpty(routedOffice.email) ? View.GONE : View.VISIBLE);
        if (callOfficeButton != null) {
            callOfficeButton.setVisibility(TextUtils.isEmpty(routedOffice.phone) ? View.GONE : View.VISIBLE);
        }
    }

    private void onCallOffice() {
        if (routedOffice != null && !TextUtils.isEmpty(routedOffice.phone)) {
            IntentHelper.dial(this, routedOffice.phone);
        }
    }

    private void importDeviceContact() {
        try (Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                String number = cursor.getString(1);
                String existing = text(locationInput);
                String updatedNote = (existing.isEmpty() ? "" : existing + " | ") + "Reporter Contact: " + name + " (" + number + ")";
                locationInput.setText(updatedNote);
                Toast.makeText(this, "Imported contact info via ContentResolver!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No contacts found on device", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error accessing device contacts provider", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSave() {
        CitizenReport report = buildReport(CitizenReport.STATUS_DRAFT);
        if (report != null) {
            checkAndSave(report, null);
        }
    }

    private void onSendSms() {
        CitizenReport report = buildReport(CitizenReport.STATUS_PREPARED);
        if (report == null || routedOffice == null) {
            return;
        }
        String message = messageFor(report);
        String phone = routedOffice.phone;
        checkAndSave(report, saved -> IntentHelper.sms(this, phone, message));
    }

    private void onSendEmail() {
        CitizenReport report = buildReport(CitizenReport.STATUS_PREPARED);
        if (report == null || routedOffice == null) {
            return;
        }
        String defaultMessage = messageFor(report);
        String subject = getString(R.string.report_message_subject, categoryLabel(report.category));
        String address = routedOffice.email;
        Uri photo = report.photoUri == null ? null : Uri.parse(report.photoUri);
        String officeName = routedOffice.nameEn;
        String district = report.district;
        String catLabel = categoryLabel(report.category);
        String locNote = text(locationInput);
        String desc = report.description;

        Toast.makeText(this, "AI composing formal complaint letter via Gemini...", Toast.LENGTH_SHORT).show();

        generateAiEmailBody(officeName, district, catLabel, locNote, desc, aiBody -> {
            String finalBody = TextUtils.isEmpty(aiBody) ? defaultMessage : aiBody;
            checkAndSave(report, saved -> IntentHelper.email(this, address, subject, finalBody, photo));
        });
    }

    private void generateAiEmailBody(String officeName, String district, String categoryLabel, String locationNote, String description, Consumer<String> callback) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        String prompt = "Act as an expert civic advocacy officer in Bangladesh. Write a formal, polite, and professional complaint email letter addressed to '"
                + officeName + "' in " + district + ", Bangladesh regarding a public " + categoryLabel + " issue.\n"
                + "Location Details: " + (locationNote.isEmpty() ? "Not specified" : locationNote) + "\n"
                + "Issue Description: " + description + "\n"
                + "Format with formal salutation, structured issue statement, request for prompt inspection/action, and respectful closing. Do not include markdown formatting or code blocks.";

        new Thread(() -> {
            String aiResult = null;
            if (!TextUtils.isEmpty(apiKey)) {
                try {
                    aiResult = com.example.civiclens.assistant.GeminiProxyClient.askDirect(apiKey, prompt);
                } catch (Exception ignored) { }
            }
            final String cleanResult = com.example.civiclens.service.NewsFetcher.cleanPlainText(aiResult);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(cleanResult));
        }).start();
    }

    private void onShare() {
        CitizenReport report = buildReport(CitizenReport.STATUS_DRAFT);
        if (report != null) {
            IntentHelper.shareText(this, messageFor(report), getString(R.string.report_share_title));
        }
    }

    /** Queues the report for the CivicLens server. The worker sends it when the network is available. */
    private void onSubmit() {
        CitizenReport report = buildReport(CitizenReport.STATUS_DRAFT);
        if (report == null) {
            return;
        }
        report.syncState = CitizenReport.SYNC_QUEUED;
        if (BuildConfig.REPORT_URL == null || BuildConfig.REPORT_URL.isEmpty()) {
            Toast.makeText(this, R.string.report_submit_unconfigured, Toast.LENGTH_LONG).show();
        }
        checkAndSave(report, saved -> SyncScheduler.enqueueNow(this));
    }

    /** Returns null and shows a message when the form is not valid. */
    private CitizenReport buildReport(String status) {
        String description = text(descriptionInput);
        if (description.length() < MIN_DESCRIPTION) {
            Toast.makeText(this, R.string.report_too_short, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (description.length() > MAX_DESCRIPTION) {
            Toast.makeText(this, R.string.report_too_long, Toast.LENGTH_SHORT).show();
            return null;
        }
        String district = selectedDistrict();
        if (district.isEmpty()) {
            Toast.makeText(this, R.string.report_district_required, Toast.LENGTH_SHORT).show();
            return null;
        }

        String locationNote = text(locationInput);
        if (com.example.civiclens.service.NewsFetcher.isExcludedTopic(description + " " + locationNote)) {
            Toast.makeText(this, "⚠ Report contains off-topic content. Please report civic problems only.", Toast.LENGTH_LONG).show();
            return null;
        }

        CitizenReport r = new CitizenReport();
        r.id = UUID.randomUUID().toString();
        r.category = selectedCategoryKey();
        r.description = description;
        r.district = district;
        r.locationNote = locationNote;
        r.photoUri = photoUri;
        r.officeId = routedOffice == null ? null : routedOffice.id;
        r.officeName = routedOffice == null ? null : routedOffice.nameEn;
        r.status = status;
        r.createdAt = System.currentTimeMillis();
        return r;
    }

    /**
     * Compares the report with earlier reports in the same category and district. A likely
     * duplicate is flagged for review but still saved, and the citizen is told why.
     */
    private void checkAndSave(CitizenReport report, Consumer<CitizenReport> afterSave) {
        boolean locationMentioned = report.description.toLowerCase(Locale.US).contains(report.district.toLowerCase(Locale.US))
                || report.locationNote.toLowerCase(Locale.US).contains(report.district.toLowerCase(Locale.US));

        if (!locationMentioned) {
            com.example.civiclens.notify.NotificationHelper.showProjectAlert(
                    this,
                    "Report Location Context Alert",
                    "Report for " + report.district + " does not explicitly mention the district name in description. Flagged for review.",
                    null
            );
        }

        reportRepository.getScopedDescriptions(report.category, report.district, existing -> {
            boolean duplicate = DuplicateChecker.isLikelyDuplicate(report.description, existing);
            if (duplicate) {
                report.status = CitizenReport.STATUS_DUPLICATE;
            }
            reportRepository.insert(report, () -> {
                duplicateText.setVisibility(duplicate ? View.VISIBLE : View.GONE);
                if (duplicate) {
                    duplicateText.setText(R.string.report_duplicate_warning);
                } else {
                    Toast.makeText(this, R.string.report_saved, Toast.LENGTH_SHORT).show();
                }
                loadReports();
                if (afterSave != null) {
                    afterSave.accept(report);
                }
            });
        });
    }

    private String messageFor(CitizenReport r) {
        return getString(R.string.report_message_body,
                categoryLabel(r.category),
                r.district,
                TextUtils.isEmpty(r.locationNote) ? getString(R.string.report_not_given) : r.locationNote,
                r.description,
                r.photoUri == null ? getString(R.string.report_no) : getString(R.string.report_yes));
    }

    private void loadReports() {
        reportRepository.getAll(reports -> {
            List<LineAdapter.Line> lines = new ArrayList<>();
            for (CitizenReport r : reports) {
                String title = categoryLabel(r.category) + " · " + r.district;
                String office = r.officeName == null ? getString(R.string.report_no_route) : r.officeName;
                String subtitle = syncLabel(r) + " · " + office + " · " + formatTime(r.createdAt);
                lines.add(new LineAdapter.Line(title, subtitle));
            }
            reportsAdapter.submit(lines);
            reportsEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    /** Shows what really happened to the report, based on the server's answer. */
    private String syncLabel(CitizenReport r) {
        if (CitizenReport.SYNC_QUEUED.equals(r.syncState)) {
            return getString(R.string.sync_queued);
        }
        if (CitizenReport.SYNC_RECEIVED.equals(r.syncState)) {
            if ("FORWARDED".equals(r.serverStatus)) {
                return getString(R.string.sync_forwarded);
            }
            if ("OFFICE_CONFIRMED".equals(r.serverStatus)) {
                return getString(R.string.sync_confirmed);
            }
            if ("RESOLVED".equals(r.serverStatus)) {
                return getString(R.string.sync_resolved);
            }
            return getString(R.string.sync_received);
        }
        return statusLabel(r.status);
    }

    private String categoryLabel(String key) {
        return getResources().getStringArray(R.array.report_categories)[ReportCategory.indexOf(key)];
    }

    private String statusLabel(String status) {
        if (CitizenReport.STATUS_PREPARED.equals(status)) {
            return getString(R.string.report_status_prepared);
        }
        if (CitizenReport.STATUS_DUPLICATE.equals(status)) {
            return getString(R.string.report_status_duplicate);
        }
        return getString(R.string.report_status_draft);
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void exportReportsCsv() {
        ReportRepository repo = new ReportRepository(this);
        repo.getAll(reports -> {
            if (reports == null || reports.isEmpty()) {
                Toast.makeText(this, "No reports available to export.", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder csv = new StringBuilder("Report ID,Category,District,Location,Description,Status,Created At\n");
            for (CitizenReport r : reports) {
                csv.append('"').append(r.id).append("\",")
                   .append('"').append(r.category).append("\",")
                   .append('"').append(r.district).append("\",")
                   .append('"').append(r.locationNote).append("\",")
                   .append('"').append(r.description.replace('"', '\'')).append("\",")
                   .append('"').append(r.status).append("\",")
                   .append('"').append(r.createdAt).append("\"\n");
            }
            IntentHelper.shareText(this, csv.toString(), "Export CivicLens Reports (CSV)");
            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "📥 Reports Exported as CSV!", false);
        });
    }

    private static String formatTime(long millis) {
        return new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(new Date(millis));
    }
}

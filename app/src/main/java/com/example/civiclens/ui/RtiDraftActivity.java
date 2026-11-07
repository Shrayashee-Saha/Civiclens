package com.example.civiclens.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;
import com.example.civiclens.model.PublicProject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RtiDraftActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private ProjectRepository repository;
    private PublicProject project;
    private TextInputEditText nameInput;
    private TextInputEditText addressInput;
    private TextInputEditText infoInput;
    private TextView resultText;
    private MaterialButton copyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rti_draft);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        repository = new ProjectRepository(this);

        nameInput = findViewById(R.id.applicantNameInput);
        addressInput = findViewById(R.id.applicantAddressInput);
        infoInput = findViewById(R.id.requestedInfoInput);
        resultText = findViewById(R.id.rtiResultText);
        copyButton = findViewById(R.id.copyRtiButton);

        MaterialButton generate = findViewById(R.id.generateRtiButton);
        generate.setOnClickListener(v -> onGenerate());
        copyButton.setOnClickListener(v -> onCopy());

        if (projectId != null) {
            repository.getProject(projectId, p -> {
                this.project = p;
                if (p != null && infoInput != null) {
                    infoInput.setText("Certified copy of approved budget breakdown, work order agreement, and physical inspection progress reports for project: " + p.name + " (" + p.id + ")");
                }
            });
        }
    }

    private void onGenerate() {
        String name = text(nameInput);
        String address = text(addressInput);
        String requestedInfo = text(infoInput);

        if (name.isEmpty() || address.isEmpty() || requestedInfo.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields to generate your RTI draft.", Toast.LENGTH_SHORT).show();
            return;
        }

        String today = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        String pName = project == null ? "Government Infrastructure Project" : project.name;
        String agency = project == null ? "Responsible Government Agency" : project.agency;

        StringBuilder sb = new StringBuilder();
        sb.append("FORM 'KA' (ফরম 'ক')\n");
        sb.append("Application for Information under Right to Information Act, 2009 (তথ্য অধিকার আইন, ২০০৯)\n\n");
        sb.append("Date: ").append(today).append("\n\n");
        sb.append("To:\n");
        sb.append("Designated Public Information Officer (পিআইও)\n");
        sb.append(agency).append("\n");
        sb.append("Bangladesh.\n\n");
        sb.append("1. Applicant Name: ").append(name).append("\n");
        sb.append("2. Address: ").append(address).append("\n");
        sb.append("3. Information Requested for Project: ").append(pName).append("\n");
        sb.append("4. Particulars of Information Required:\n   - ").append(requestedInfo).append("\n\n");
        sb.append("5. Preferred Mode of Receipt: Certified Photocopy / Digital Inspection\n");
        sb.append("6. I hereby state that the requested information pertains to public interest and public infrastructure transparency.\n\n");
        sb.append("Signature of Applicant:\n");
        sb.append(name).append("\n");

        resultText.setText(sb.toString());
        resultText.setVisibility(View.VISIBLE);
        copyButton.setVisibility(View.VISIBLE);
    }

    private void onCopy() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("RTI Draft", resultText.getText().toString());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "RTI application draft copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}

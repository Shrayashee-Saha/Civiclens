package com.example.civiclens.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;

public class AddProjectActivity extends AppCompatActivity {

    private ProjectRepository repository;
    private TextInputEditText nameInput;
    private TextInputEditText urlInput;
    private TextInputEditText districtInput;
    private TextInputEditText upazilaInput;
    private TextInputEditText notesInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        repository = new ProjectRepository(this);
        nameInput = findViewById(R.id.projectNameInput);
        urlInput = findViewById(R.id.newsUrlInput);
        districtInput = findViewById(R.id.districtInput);
        upazilaInput = findViewById(R.id.upazilaInput);
        notesInput = findViewById(R.id.notesInput);

        MaterialButton submit = findViewById(R.id.submitContributionButton);
        submit.setOnClickListener(v -> onSubmit());
    }

    private void onSubmit() {
        String name = text(nameInput);
        String url = text(urlInput);
        String district = text(districtInput);
        String upazila = text(upazilaInput);
        String notes = text(notesInput);

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a project name.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a news article or official source URL.", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.submitProjectContribution(name, url, district, upazila, notes, () -> {
            Toast.makeText(this, "Project submitted! Pending source verification by moderator.", Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}

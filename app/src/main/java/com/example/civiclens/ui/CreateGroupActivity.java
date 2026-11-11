package com.example.civiclens.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;
import com.example.civiclens.data.CommunityRepository;

public class CreateGroupActivity extends AppCompatActivity {

    private CommunityRepository repository;
    private TextInputEditText nameInput;
    private TextInputEditText descInput;
    private TextInputEditText districtInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        repository = new CommunityRepository(this);
        nameInput = findViewById(R.id.groupNameInput);
        descInput = findViewById(R.id.groupDescInput);
        districtInput = findViewById(R.id.groupDistrictInput);

        MaterialButton submitBtn = findViewById(R.id.btnSubmitCreateGroup);
        submitBtn.setOnClickListener(v -> onSubmit());
    }

    private void onSubmit() {
        String name = text(nameInput);
        String desc = text(descInput);
        String district = text(districtInput);

        if (name.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please enter group name and description.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.civiclens.cloud.GroupCloudRepository.createGroup(this, name, desc, district, "Public Community", (success, message, data) -> {
            repository.createCommunity(name, desc, "Environment", district, "Public Community", () -> {
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "🌱 Civic Community Created & Published!", false);
                finish();
            });
        });
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

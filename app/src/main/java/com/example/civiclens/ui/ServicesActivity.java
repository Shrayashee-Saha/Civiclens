package com.example.civiclens.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.domain.SearchRanker;
import com.example.civiclens.model.GovernmentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated Full-Screen Directory for Government Services (NID, Birth Certificate, Education, Passport, etc.)
 */
public class ServicesActivity extends AppCompatActivity {

    private ServiceAdapter adapter;
    private TextView emptyText;
    private TextInputEditText searchInput;
    private List<GovernmentService> allServices = new ArrayList<>();
    private String currentQuery = "";
    private String selectedCategory = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        emptyText = findViewById(R.id.emptyText);
        searchInput = findViewById(R.id.searchInput);

        RecyclerView list = findViewById(R.id.servicesList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(service ->
                startActivity(ServiceDetailActivity.intent(this, service.id)));
        list.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                currentQuery = s.toString();
                render();
            }
        });

        bindChip(R.id.chipAll, "ALL");
        bindChip(R.id.chipIdentity, "Identity");
        bindChip(R.id.chipEducation, "Education");
        bindChip(R.id.chipBusiness, "Business");
        bindChip(R.id.chipLand, "Land");
        bindChip(R.id.chipUtilities, "Utilities");

        new ServiceRepository(this).getAllServices(services -> {
            if (isFinishing()) return;
            allServices = services;
            render();
        });
    }

    private void bindChip(int resId, String category) {
        MaterialButton btn = findViewById(resId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                selectedCategory = category;
                render();
            });
        }
    }

    private void render() {
        List<GovernmentService> ranked = SearchRanker.rank(currentQuery, allServices);
        List<GovernmentService> filtered = new ArrayList<>();

        for (GovernmentService s : ranked) {
            if ("ALL".equalsIgnoreCase(selectedCategory)) {
                filtered.add(s);
            } else if (s.category != null && s.category.toLowerCase().contains(selectedCategory.toLowerCase())) {
                filtered.add(s);
            } else if (s.nameEn != null && s.nameEn.toLowerCase().contains(selectedCategory.toLowerCase())) {
                filtered.add(s);
            }
        }

        adapter.submit(filtered);
        emptyText.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

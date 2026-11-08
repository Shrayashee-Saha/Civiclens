package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.domain.SearchRanker;
import com.example.civiclens.model.GovernmentService;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ServiceAdapter adapter;
    private TextView emptyText;
    private TextInputEditText searchInput;
    private List<GovernmentService> allServices = new ArrayList<>();
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyText = view.findViewById(R.id.emptyText);
        searchInput = view.findViewById(R.id.searchInput);

        RecyclerView results = view.findViewById(R.id.resultsList);
        results.setLayoutManager(new LinearLayoutManager(requireContext()));
        results.setNestedScrollingEnabled(false);
        adapter = new ServiceAdapter(service ->
                startActivity(ServiceDetailActivity.intent(requireContext(), service.id)));
        results.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                currentQuery = s.toString();
                render();
            }
        });

        MaterialCardView quickFindCard = view.findViewById(R.id.quickFind);
        if (quickFindCard != null) {
            quickFindCard.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), ServicesActivity.class)));
        }

        bindQuickAction(view, R.id.quickOffices, R.id.nav_offices);
        bindQuickAction(view, R.id.quickSaved, R.id.nav_my_services);

        MaterialCardView quickProjectsCard = view.findViewById(R.id.quickProjects);
        if (quickProjectsCard != null) {
            quickProjectsCard.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), ProjectsActivity.class)));
        }

        MaterialButton ask = view.findViewById(R.id.askButton);
        ask.setOnClickListener(v -> startActivity(new Intent(requireContext(), AskActivity.class)));

        MaterialButton report = view.findViewById(R.id.reportButton);
        report.setOnClickListener(v -> startActivity(new Intent(requireContext(), ReportActivity.class)));

        MaterialButton accWatch = view.findViewById(R.id.accountabilityWatchButton);
        if (accWatch != null) {
            accWatch.setOnClickListener(v -> startActivity(new Intent(requireContext(), AccountabilityActivity.class)));
        }

        MaterialButton contribute = view.findViewById(R.id.contributeProjectButton);
        if (contribute != null) {
            contribute.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddProjectActivity.class)));
        }

        MaterialButton execDash = view.findViewById(R.id.execDashboardButton);
        if (execDash != null) {
            execDash.setOnClickListener(v -> startActivity(new Intent(requireContext(), DashboardActivity.class)));
        }

        new ServiceRepository(requireContext()).getAllServices(list -> {
            if (!isAdded()) {
                return;
            }
            allServices = list;
            render();
        });
    }

    private void bindQuickAction(View root, int cardId, Integer tabId) {
        MaterialCardView card = root.findViewById(cardId);
        card.setOnClickListener(v -> {
            if (tabId == null) {
                searchInput.requestFocus();
            } else if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(tabId);
            }
        });
    }

    private void render() {
        if (currentQuery == null || currentQuery.trim().isEmpty()) {
            adapter.submit(new ArrayList<>());
            emptyText.setVisibility(View.GONE);
            return;
        }
        List<GovernmentService> results = SearchRanker.rank(currentQuery, allServices);
        adapter.submit(results);
        emptyText.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

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

    private LineAdapter universalAdapter;
    private TextView emptyText;
    private TextInputEditText searchInput;
    private List<GovernmentService> allServices = new ArrayList<>();
    private final List<SearchResultItem> lastSearchHits = new ArrayList<>();
    private String currentQuery = "";

    private static class SearchResultItem {
        final String title;
        final String subtitle;
        final Runnable onClick;

        SearchResultItem(String title, String subtitle, Runnable onClick) {
            this.title = title;
            this.subtitle = subtitle;
            this.onClick = onClick;
        }
    }

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
        universalAdapter = new LineAdapter(position -> {
            if (position >= 0 && position < lastSearchHits.size()) {
                lastSearchHits.get(position).onClick.run();
            }
        });
        results.setAdapter(universalAdapter);

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

        MaterialButton helplines = view.findViewById(R.id.helplinesButton);
        if (helplines != null) {
            helplines.setOnClickListener(v -> startActivity(new Intent(requireContext(), HelplinesActivity.class)));
        }

        MaterialButton mapExplorer = view.findViewById(R.id.mapExplorerButton);
        if (mapExplorer != null) {
            mapExplorer.setOnClickListener(v -> startActivity(new Intent(requireContext(), DivisionMapActivity.class)));
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
            List<LineAdapter.Line> lines = new ArrayList<>();
            lastSearchHits.clear();
            for (GovernmentService s : allServices) {
                lines.add(new LineAdapter.Line(s.nameEn, "[Service] · " + s.authority + " · " + s.category));
                lastSearchHits.add(new SearchResultItem(s.nameEn, s.authority, () ->
                        startActivity(ServiceDetailActivity.intent(requireContext(), s.id))));
            }
            universalAdapter.submit(lines);
            emptyText.setVisibility(View.GONE);
            return;
        }

        String queryLower = currentQuery.toLowerCase().trim();
        List<LineAdapter.Line> lines = new ArrayList<>();
        lastSearchHits.clear();

        for (GovernmentService s : allServices) {
            if (s.nameEn.toLowerCase().contains(queryLower) || s.category.toLowerCase().contains(queryLower) || (s.keywords != null && s.keywords.toLowerCase().contains(queryLower))) {
                lines.add(new LineAdapter.Line("📌 " + s.nameEn, "[Service] · " + s.authority));
                lastSearchHits.add(new SearchResultItem(s.nameEn, s.authority, () ->
                        startActivity(ServiceDetailActivity.intent(requireContext(), s.id))));
            }
        }

        new ServiceRepository(requireContext()).getOffices(offices -> {
            if (!isAdded()) return;
            for (com.example.civiclens.model.GovernmentOffice o : offices) {
                if (o.nameEn.toLowerCase().contains(queryLower) || o.district.toLowerCase().contains(queryLower)) {
                    lines.add(new LineAdapter.Line("🏛️ " + o.nameEn, "[Office Directory] · " + o.district + " · Email: " + o.email));
                    lastSearchHits.add(new SearchResultItem(o.nameEn, o.district, () -> {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).selectTab(R.id.nav_offices);
                        }
                    }));
                }
            }

            new com.example.civiclens.data.ProjectRepository(requireContext()).getProjects(projects -> {
                if (!isAdded()) return;
                for (com.example.civiclens.model.PublicProject p : projects) {
                    if (p.name.toLowerCase().contains(queryLower) || p.district.toLowerCase().contains(queryLower)) {
                        lines.add(new LineAdapter.Line("🏗️ " + p.name, "[Public Project] · " + p.district + " · Budget BDT " + (p.budgetBdt / 10000000L) + " Cr"));
                        lastSearchHits.add(new SearchResultItem(p.name, p.district, () ->
                                startActivity(ProjectDetailActivity.intent(requireContext(), p.id))));
                    }
                }

                universalAdapter.submit(lines);
                emptyText.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }
}

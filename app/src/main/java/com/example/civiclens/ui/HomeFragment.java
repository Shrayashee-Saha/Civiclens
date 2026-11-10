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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;
import com.example.civiclens.data.AppPrefs;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.model.GovernmentService;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private LineAdapter universalAdapter;
    private TextView emptyText;
    private RecyclerView resultsList;
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
        resultsList = view.findViewById(R.id.resultsList);
        searchInput = view.findViewById(R.id.searchInput);

        if (resultsList != null) {
            resultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            resultsList.setNestedScrollingEnabled(false);
            universalAdapter = new LineAdapter(position -> {
                if (position >= 0 && position < lastSearchHits.size()) {
                    lastSearchHits.get(position).onClick.run();
                }
            });
            resultsList.setAdapter(universalAdapter);
        }

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override public void afterTextChanged(Editable s) {
                    currentQuery = s.toString();
                    renderSpotlightSearch();
                }
            });
        }

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

        MaterialButton feedBtn = view.findViewById(R.id.civicFeedButton);
        if (feedBtn != null) {
            feedBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), CivicFeedActivity.class)));
        }

        MaterialButton commsBtn = view.findViewById(R.id.btnCivicCommunities);
        if (commsBtn != null) {
            commsBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), CivicCommunitiesActivity.class)));
        }

        new ServiceRepository(requireContext()).getAllServices(list -> {
            if (!isAdded()) return;
            allServices = list;
        });
    }

    private void renderSpotlightSearch() {
        if (resultsList == null || universalAdapter == null) return;

        if (currentQuery == null || currentQuery.trim().isEmpty()) {
            resultsList.setVisibility(View.GONE);
            if (emptyText != null) emptyText.setVisibility(View.GONE);
            return;
        }

        resultsList.setVisibility(View.VISIBLE);
        String queryLower = currentQuery.toLowerCase().trim();
        List<LineAdapter.Line> lines = new ArrayList<>();
        lastSearchHits.clear();

        // 1. Settings & App Actions
        if ("dark mode".contains(queryLower) || "night".contains(queryLower) || "theme".contains(queryLower)) {
            lines.add(new LineAdapter.Line("🌙 Toggle Dark Theme / Night Mode", "[Setting Action] · Tap to switch app dark/light mode"));
            lastSearchHits.add(new SearchResultItem("Dark Mode", "Setting Action", () -> {
                boolean newDark = !AppPrefs.isDarkMode(requireContext());
                AppPrefs.setDarkMode(requireContext(), newDark);
                AppCompatDelegate.setDefaultNightMode(newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }));
        }

        if ("vault".contains(queryLower) || "wallet".contains(queryLower) || "nid card".contains(queryLower) || "license".contains(queryLower)) {
            lines.add(new LineAdapter.Line("🔐 Digital Citizen Vault (Encrypted Wallet)", "[Feature Action] · Access NID & License Wallet"));
            lastSearchHits.add(new SearchResultItem("Vault", "Feature", () ->
                    startActivity(new Intent(requireContext(), VaultActivity.class))));
        }

        if ("notification".contains(queryLower) || "alert".contains(queryLower) || "inbox".contains(queryLower)) {
            lines.add(new LineAdapter.Line("📬 Notification Inbox & Case Tracker", "[Feature Action] · View real-time alerts & status"));
            lastSearchHits.add(new SearchResultItem("Inbox", "Feature", () ->
                    startActivity(new Intent(requireContext(), NotificationsActivity.class))));
        }

        if ("ask".contains(queryLower) || "ai".contains(queryLower) || "chat".contains(queryLower) || "gemini".contains(queryLower)) {
            lines.add(new LineAdapter.Line("✨ Ask CivicLens AI Assistant", "[Feature Action] · Interactive AI Chat & Voice Assistant"));
            lastSearchHits.add(new SearchResultItem("Ask AI", "Feature", () ->
                    startActivity(new Intent(requireContext(), AskActivity.class))));
        }

        if ("report".contains(queryLower) || "complaint".contains(queryLower) || "problem".contains(queryLower)) {
            lines.add(new LineAdapter.Line("🚨 Report a Local Civic Problem", "[Feature Action] · File complaint with GPS pinpoint"));
            lastSearchHits.add(new SearchResultItem("Report Problem", "Feature", () ->
                    startActivity(new Intent(requireContext(), ReportActivity.class))));
        }

        if ("dashboard".contains(queryLower) || "executive".contains(queryLower) || "cag".contains(queryLower)) {
            lines.add(new LineAdapter.Line("📊 Executive Civic Intelligence Dashboard", "[Feature Action] · CAG Audit Flags & PDF Report"));
            lastSearchHits.add(new SearchResultItem("Executive Dashboard", "Feature", () ->
                    startActivity(new Intent(requireContext(), DashboardActivity.class))));
        }

        if ("helpline".contains(queryLower) || "999".contains(queryLower) || "333".contains(queryLower) || "106".contains(queryLower) || "emergency".contains(queryLower)) {
            lines.add(new LineAdapter.Line("📞 Emergency & Civic Helplines Hub (999, 333, 106, 16263)", "[Emergency Action] · One-Tap Toll-Free Dialer"));
            lastSearchHits.add(new SearchResultItem("Helplines", "Emergency Action", () ->
                    startActivity(new Intent(requireContext(), HelplinesActivity.class))));
        }

        if ("map".contains(queryLower) || "division".contains(queryLower) || "dhaka".contains(queryLower) || "sylhet".contains(queryLower)) {
            lines.add(new LineAdapter.Line("🗺️ Bangladesh Divisional Map Explorer", "[Feature Action] · Inspect 8 Divisions & Offices"));
            lastSearchHits.add(new SearchResultItem("Map Explorer", "Feature", () ->
                    startActivity(new Intent(requireContext(), DivisionMapActivity.class))));
        }

        if ("rti".contains(queryLower) || "form ka".contains(queryLower) || "information".contains(queryLower)) {
            lines.add(new LineAdapter.Line("📄 Right to Information (RTI Form Ka) Assistant", "[Legal Draft Action] · Generate Official Request PDF"));
            lastSearchHits.add(new SearchResultItem("RTI Assistant", "Legal Action", () ->
                    startActivity(new Intent(requireContext(), RtiDraftActivity.class))));
        }

        // 2. Database Records (Services)
        for (GovernmentService s : allServices) {
            if (s.nameEn.toLowerCase().contains(queryLower) || s.category.toLowerCase().contains(queryLower)) {
                lines.add(new LineAdapter.Line("📌 " + s.nameEn, "[Service Record] · " + s.authority));
                lastSearchHits.add(new SearchResultItem(s.nameEn, s.authority, () ->
                        startActivity(ServiceDetailActivity.intent(requireContext(), s.id))));
            }
        }

        universalAdapter.submit(lines);
        if (emptyText != null) {
            emptyText.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void bindQuickAction(View root, int cardId, Integer tabId) {
        MaterialCardView card = root.findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                if (tabId == null) {
                    startActivity(new Intent(requireContext(), ServicesActivity.class));
                } else if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(tabId);
                }
            });
        }
    }
}

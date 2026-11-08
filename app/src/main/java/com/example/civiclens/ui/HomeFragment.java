package com.example.civiclens.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;

public class HomeFragment extends Fragment {

    private TextInputEditText searchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.searchInput);
        if (searchInput != null) {
            searchInput.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), ServicesActivity.class)));
            searchInput.setFocusable(false);
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

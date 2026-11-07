package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.civiclens.R;
import com.example.civiclens.data.ProjectRepository;
import com.example.civiclens.domain.ProjectAnalyzer;
import com.example.civiclens.model.PublicProject;

import java.util.ArrayList;
import java.util.List;

/** Browse projects by district and category. All records are labelled as fictional. */
public class ProjectsFragment extends Fragment {

    private ProjectRepository repository;
    private LineAdapter adapter;
    private TextView emptyText;
    private List<PublicProject> all = new ArrayList<>();
    private List<PublicProject> visible = new ArrayList<>();
    private final List<String> districtValues = new ArrayList<>();
    private final List<String> categoryValues = new ArrayList<>();
    private String selectedDistrict = "";
    private String selectedCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ProjectRepository(requireContext());
        emptyText = view.findViewById(R.id.emptyText);

        RecyclerView list = view.findViewById(R.id.projectList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LineAdapter(position -> {
            if (position >= 0 && position < visible.size()) {
                startActivity(ProjectDetailActivity.intent(requireContext(), visible.get(position).id));
            }
        });
        list.setAdapter(adapter);

        Spinner districtSpinner = view.findViewById(R.id.districtSpinner);
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                selectedDistrict = districtValues.get(position);
                render();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Spinner categorySpinner = view.findViewById(R.id.categorySpinner);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                selectedCategory = categoryValues.get(position);
                render();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        View imedBtn = view.findViewById(R.id.btnImedReports);
        if (imedBtn != null) {
            imedBtn.setOnClickListener(v -> IntentHelper.openUrl(requireContext(), "https://imed.gov.bd/pages/reports"));
        }

        View planBtn = view.findViewById(R.id.btnPlanningMinistry);
        if (planBtn != null) {
            planBtn.setOnClickListener(v -> IntentHelper.openUrl(requireContext(), "https://objectstorage.ap-dcc-gazipur-1.oraclecloud15.com/n/axvjbnqprylg/b/V2Ministry/o/office-plandiv/2024/12/a8dd76d8d2e64fc3b6ab245a0034349f.pdf"));
        }

        View unbBtn = view.findViewById(R.id.btnUnbReport);
        if (unbBtn != null) {
            unbBtn.setOnClickListener(v -> IntentHelper.openUrl(requireContext(), "https://unb.com.bd/category/Bangladesh/adp-implementation-drops-to-389pc-in-july-september/198705"));
        }

        View autoSurveyBtn = view.findViewById(R.id.btnAutoSurvey);
        if (autoSurveyBtn != null) {
            autoSurveyBtn.setOnClickListener(v -> {
                android.widget.Toast.makeText(requireContext(), "Scanning live gazettes & news media streams...", android.widget.Toast.LENGTH_SHORT).show();
                repository.runAutoSurvey(count -> {
                    if (!isAdded()) return;
                    if (count > 0) {
                        android.widget.Toast.makeText(requireContext(), "Auto-Survey Complete! Discovered " + count + " new project(s).", android.widget.Toast.LENGTH_LONG).show();
                        loadProjects(districtSpinner, categorySpinner);
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Auto-Survey Complete! All public project records are up to date.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        loadProjects(districtSpinner, categorySpinner);
    }

    private void loadProjects(Spinner districtSpinner, Spinner categorySpinner) {
        repository.getProjects(projects -> {
            if (!isAdded()) {
                return;
            }
            all = projects;
            buildSpinner(districtSpinner, districtValues, R.string.all_districts,
                    ProjectAnalyzer.distinct(all, true));
            buildSpinner(categorySpinner, categoryValues, R.string.all_categories,
                    ProjectAnalyzer.distinct(all, false));
            render();
        });
    }

    private void buildSpinner(Spinner spinner, List<String> values, int allLabel, List<String> options) {
        values.clear();
        values.add("");
        values.addAll(options);

        List<String> labels = new ArrayList<>();
        labels.add(getString(allLabel));
        labels.addAll(options);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void render() {
        visible = ProjectAnalyzer.filter(selectedDistrict, selectedCategory, all);
        List<LineAdapter.Line> lines = new ArrayList<>();
        for (PublicProject p : visible) {
            lines.add(new LineAdapter.Line(p.name,
                    getString(R.string.project_agency_format, p.agency)
                            + " · " + p.district + " · " + p.category));
        }
        adapter.submit(lines);
        emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

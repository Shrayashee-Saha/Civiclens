package com.example.civiclens.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.domain.OfficeFinder;
import com.example.civiclens.model.GovernmentOffice;
import com.example.civiclens.model.UserSavedService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OfficesFragment extends Fragment {

    private ServiceRepository repository;
    private OfficeAdapter adapter;
    private TextView emptyText;
    private List<GovernmentOffice> allOffices = new ArrayList<>();
    private final List<String> districtOptions = new ArrayList<>();
    private Set<String> savedIds = new HashSet<>();
    private String query = "";
    private String selectedDistrict = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ServiceRepository(requireContext());
        emptyText = view.findViewById(R.id.emptyText);

        RecyclerView list = view.findViewById(R.id.officeList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OfficeAdapter(new OfficeAdapter.Actions() {
            @Override
            public void onNavigate(GovernmentOffice office) {
                IntentHelper.openMap(requireContext(), office);
            }

            @Override
            public void onCall(GovernmentOffice office) {
                IntentHelper.dial(requireContext(), office.phone);
            }

            @Override
            public void onMessage(GovernmentOffice office) {
                IntentHelper.sms(requireContext(), office.phone, null);
            }

            @Override
            public void onToggleSave(GovernmentOffice office, boolean save) {
                repository.setSaved(UserSavedService.KIND_OFFICE, office.id, save);
                if (save) {
                    savedIds.add(office.id);
                } else {
                    savedIds.remove(office.id);
                }
                render();
            }
        });
        list.setAdapter(adapter);

        TextInputEditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                query = s.toString();
                render();
            }
        });

        Spinner districtSpinner = view.findViewById(R.id.districtSpinner);
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                selectedDistrict = position == 0 ? "" : districtOptions.get(position);
                render();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        repository.getOffices(offices -> {
            if (!isAdded()) {
                return;
            }
            allOffices = offices;
            buildDistrictSpinner(districtSpinner);
            repository.getSavedIds(UserSavedService.KIND_OFFICE, ids -> {
                if (!isAdded()) {
                    return;
                }
                savedIds = ids;
                render();
            });
        });
    }

    private void buildDistrictSpinner(Spinner spinner) {
        districtOptions.clear();
        districtOptions.add("");
        districtOptions.addAll(OfficeFinder.districts(allOffices));

        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.all_districts));
        labels.addAll(districtOptions.subList(1, districtOptions.size()));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void render() {
        List<GovernmentOffice> results = OfficeFinder.search(query, selectedDistrict, allOffices);
        adapter.submit(results, savedIds);
        emptyText.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

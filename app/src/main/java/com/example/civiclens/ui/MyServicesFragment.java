package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.civiclens.R;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.model.GovernmentOffice;
import com.example.civiclens.model.GovernmentService;
import com.example.civiclens.model.UserSavedService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Bookmarked services and offices. Reloads every time the screen is shown. */
public class MyServicesFragment extends Fragment {

    private ServiceRepository repository;
    private ServiceAdapter serviceAdapter;
    private OfficeAdapter officeAdapter;
    private TextView serviceEmpty;
    private TextView officeEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ServiceRepository(requireContext());
        serviceEmpty = view.findViewById(R.id.serviceEmpty);
        officeEmpty = view.findViewById(R.id.officeEmpty);

        RecyclerView services = view.findViewById(R.id.servicesList);
        services.setLayoutManager(new LinearLayoutManager(requireContext()));
        services.setNestedScrollingEnabled(false);
        serviceAdapter = new ServiceAdapter(s ->
                startActivity(ServiceDetailActivity.intent(requireContext(), s.id)));
        services.setAdapter(serviceAdapter);

        RecyclerView offices = view.findViewById(R.id.officesList);
        offices.setLayoutManager(new LinearLayoutManager(requireContext()));
        offices.setNestedScrollingEnabled(false);
        officeAdapter = new OfficeAdapter(new OfficeAdapter.Actions() {
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
                loadOffices(); // reload after the write, which runs first on the same queue
            }
        });
        offices.setAdapter(officeAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadServices();
        loadOffices();
    }

    private void loadServices() {
        repository.getSavedServices(list -> {
            if (!isAdded()) {
                return;
            }
            showServices(list);
        });
    }

    private void loadOffices() {
        repository.getSavedOffices(list -> {
            if (!isAdded()) {
                return;
            }
            Set<String> ids = new HashSet<>();
            for (GovernmentOffice o : list) {
                ids.add(o.id);
            }
            officeAdapter.submit(list, ids);
            officeEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showServices(List<GovernmentService> list) {
        serviceAdapter.submit(list);
        serviceEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

package com.example.civiclens.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.civiclens.R;

/**
 * Divisional Map & Public Infrastructure Explorer
 */
public class DivisionMapActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView detailsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_division_map);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        titleText = findViewById(R.id.mapDivisionName);
        detailsText = findViewById(R.id.mapDivisionDetails);

        findViewById(R.id.btnDhaka).setOnClickListener(v -> selectDivision("Dhaka Division", "• Districts: 13 Districts (Dhaka, Gazipur, Narayanganj, Tangail, Faridpur)\n• Active Projects: 8 ADP Mega Projects (Elevated Expressway, Airport Expansion, PEDP-5)\n• DC Office Email: dc.dhaka@mopa.gov.bd"));
        findViewById(R.id.btnChattogram).setOnClickListener(v -> selectDivision("Chattogram Division", "• Districts: 11 Districts (Chattogram, Cox's Bazar, Cumilla, Feni, Noakhali)\n• Active Projects: Patenga Flyover & Coastal Embankment Project\n• DC Office Email: dc.chittagong@mopa.gov.bd"));
        findViewById(R.id.btnSylhet).setOnClickListener(v -> selectDivision("Sylhet Division", "• Districts: 4 Districts (Sylhet, Moulvibazar, Habiganj, Sunamganj)\n• Active Projects: Sylhet-Tamabil 4-Lane Highway & Flood Prevention Embankment\n• DC Office Email: dc.sylhet@mopa.gov.bd"));
        findViewById(R.id.btnKhulna).setOnClickListener(v -> selectDivision("Khulna Division", "• Districts: 10 Districts (Khulna, Bagerhat, Jessore, Kushtia, Satkhira)\n• Active Projects: Mongla Port Modernization & Dumuria Road\n• DC Office Email: dc.khulna@mopa.gov.bd"));
        findViewById(R.id.btnBarisal).setOnClickListener(v -> selectDivision("Barisal Division", "• Districts: 6 Districts (Barisal, Bhola, Jhalokathi, Patuakhali, Pirojpur)\n• Active Projects: Coastal Resilience & City Canal Re-Excavation\n• DC Office Email: dc.barisal@mopa.gov.bd"));
    }

    private void selectDivision(String name, String details) {
        com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
        titleText.setText(name);
        detailsText.setText(details);
        com.example.civiclens.accessibility.AccessibilityHelper.speak(this, name + ". " + details);
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

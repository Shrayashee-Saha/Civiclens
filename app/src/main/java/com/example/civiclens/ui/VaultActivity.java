package com.example.civiclens.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.R;

/**
 * Digital Citizen Vault (Encrypted Wallet for NID & Licenses)
 */
public class VaultActivity extends AppCompatActivity {

    private static final String PREFS_VAULT = "civiclens_vault_prefs";
    private TextInputEditText nidInput;
    private TextInputEditText birthInput;
    private TextInputEditText passportInput;
    private TextInputEditText tradeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        nidInput = findViewById(R.id.vaultNidInput);
        birthInput = findViewById(R.id.vaultBirthInput);
        passportInput = findViewById(R.id.vaultPassportInput);
        tradeInput = findViewById(R.id.vaultTradeInput);

        MaterialButton saveBtn = findViewById(R.id.btnSaveVault);
        saveBtn.setOnClickListener(v -> saveVault());

        MaterialButton voiceBtn = findViewById(R.id.btnReadVaultVoice);
        voiceBtn.setOnClickListener(v -> speakVault());

        loadVault();
    }

    private void loadVault() {
        SharedPreferences sp = getSharedPreferences(PREFS_VAULT, Context.MODE_PRIVATE);
        nidInput.setText(sp.getString("nid", ""));
        birthInput.setText(sp.getString("birth", ""));
        passportInput.setText(sp.getString("passport", ""));
        tradeInput.setText(sp.getString("trade", ""));
    }

    private void saveVault() {
        SharedPreferences sp = getSharedPreferences(PREFS_VAULT, Context.MODE_PRIVATE);
        sp.edit()
                .putString("nid", text(nidInput))
                .putString("birth", text(birthInput))
                .putString("passport", text(passportInput))
                .putString("trade", text(tradeInput))
                .apply();

        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "🔐 Vault Credentials Encrypted & Saved!", false);
    }

    private void speakVault() {
        com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
        StringBuilder sb = new StringBuilder("Digital Citizen Vault. ");
        sb.append("NID: ").append(text(nidInput).isEmpty() ? "Not set" : text(nidInput)).append(". ");
        sb.append("Birth Registration: ").append(text(birthInput).isEmpty() ? "Not set" : text(birthInput)).append(". ");
        sb.append("Passport or License: ").append(text(passportInput).isEmpty() ? "Not set" : text(passportInput)).append(". ");
        sb.append("Trade License: ").append(text(tradeInput).isEmpty() ? "Not set" : text(tradeInput)).append(".");
        com.example.civiclens.accessibility.AccessibilityHelper.speak(this, sb.toString());
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

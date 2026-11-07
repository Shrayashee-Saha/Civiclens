package com.example.civiclens.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.R;
import com.example.civiclens.auth.AuthRepository;
import com.example.civiclens.data.AppPrefs;
import com.example.civiclens.firebase.FirebaseGate;
import com.example.civiclens.notify.NotificationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

/**
 * Settings: account, preferences, language and app information.
 * Language is changed inside the app. Nothing here opens another app's settings screen.
 */
public class SettingsFragment extends Fragment {

    private static final int ROW_ACCOUNT = 0;
    private static final int ROW_NOTIFICATIONS = 1;
    private static final int ROW_FIREFOX = 2;
    private static final int ROW_LANGUAGE = 3;
    private static final int ROW_SHARE = 4;
    private static final int ROW_ABOUT = 5;
    private static final int ROW_VERSION = 6;

    private static final String[] LANGUAGE_TAGS = {"", "en", "bn"};

    private static final class Row {
        final String title;
        final String subtitle;

        Row(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private ListView list;
    private ArrayAdapter<Row> adapter;
    private TextView headerInitial;
    private TextView headerName;
    private TextView headerDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = view.findViewById(R.id.settingsList);

        View header = getLayoutInflater().inflate(R.layout.settings_header, list, false);
        headerInitial = header.findViewById(R.id.profileInitial);
        headerName = header.findViewById(R.id.profileName);
        headerDetail = header.findViewById(R.id.profileDetail);
        list.addHeaderView(header, null, false);

        adapter = new ArrayAdapter<Row>(requireContext(),
                android.R.layout.simple_list_item_2, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = convertView;
                if (row == null) {
                    row = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                Row item = getItem(position);
                if (item != null) {
                    ((TextView) row.findViewById(android.R.id.text1)).setText(item.title);
                    ((TextView) row.findViewById(android.R.id.text2)).setText(item.subtitle);
                }
                row.setMinimumHeight((int) (64 * getResources().getDisplayMetrics().density));
                return row;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, v, position, id) ->
                onRowClicked(position - list.getHeaderViewsCount()));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRows();
    }

    private void refreshRows() {
        Context context = requireContext();
        boolean firebaseReady = FirebaseGate.isReady(context);
        String email = firebaseReady ? new AuthRepository().currentEmail() : null;

        // Header card
        if (!firebaseReady) {
            headerName.setText(R.string.settings_guest);
            headerDetail.setText(R.string.setting_account_unavailable);
            headerInitial.setText("C");
        } else if (email == null) {
            headerName.setText(R.string.settings_guest);
            headerDetail.setText(R.string.setting_account_signed_out);
            headerInitial.setText("G");
        } else {
            headerName.setText(email);
            headerDetail.setText(R.string.setting_account_signed_in);
            headerInitial.setText(email.substring(0, 1).toUpperCase());
        }

        // Account row
        String accountSubtitle;
        if (!firebaseReady) {
            accountSubtitle = getString(R.string.setting_account_unavailable);
        } else if (email == null) {
            accountSubtitle = getString(R.string.setting_account_signed_out);
        } else {
            accountSubtitle = email;
        }

        boolean notifications = NotificationHelper.remindersEnabled(context);
        boolean firefox = AppPrefs.preferFirefox(context);
        boolean firefoxInstalled = IntentHelper.isInstalled(context, IntentHelper.FIREFOX_PACKAGE);

        String firefoxSubtitle;
        if (!firefox) {
            firefoxSubtitle = getString(R.string.setting_firefox_off);
        } else if (firefoxInstalled) {
            firefoxSubtitle = getString(R.string.setting_firefox_on);
        } else {
            firefoxSubtitle = getString(R.string.setting_firefox_missing);
        }

        adapter.clear();
        adapter.add(new Row(getString(R.string.setting_account), accountSubtitle));
        adapter.add(new Row(getString(R.string.setting_notifications),
                getString(notifications ? R.string.setting_notifications_on : R.string.setting_notifications_off)));
        adapter.add(new Row(getString(R.string.setting_firefox), firefoxSubtitle));
        adapter.add(new Row(getString(R.string.setting_language),
                getString(R.string.setting_language_current, currentLanguageLabel())));
        adapter.add(new Row(getString(R.string.setting_share), getString(R.string.setting_share_subtitle)));
        adapter.add(new Row(getString(R.string.setting_about), getString(R.string.setting_about_subtitle)));
        adapter.add(new Row(getString(R.string.setting_version),
                getString(R.string.setting_version_format, BuildConfig.VERSION_NAME)));
        adapter.notifyDataSetChanged();
    }

    private String currentLanguageLabel() {
        String tag = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (tag == null || tag.isEmpty()) {
            return getString(R.string.settings_lang_system);
        }
        return tag.startsWith("bn") ? getString(R.string.settings_lang_bangla)
                : getString(R.string.settings_lang_english);
    }

    private void onRowClicked(int position) {
        switch (position) {
            case ROW_ACCOUNT:
                onAccountClicked();
                break;
            case ROW_NOTIFICATIONS:
                NotificationHelper.setRemindersEnabled(requireContext(),
                        !NotificationHelper.remindersEnabled(requireContext()));
                refreshRows();
                break;
            case ROW_FIREFOX:
                AppPrefs.setPreferFirefox(requireContext(), !AppPrefs.preferFirefox(requireContext()));
                refreshRows();
                break;
            case ROW_LANGUAGE:
                showLanguageDialog();
                break;
            case ROW_SHARE:
                IntentHelper.shareText(requireContext(),
                        getString(R.string.share_message_app), getString(R.string.share_chooser));
                break;
            case ROW_ABOUT:
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.setting_about)
                        .setMessage(R.string.about_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                break;
            default:
                break;
        }
    }

    private void onAccountClicked() {
        Context context = requireContext();
        if (!FirebaseGate.isReady(context)) {
            Toast.makeText(context, R.string.setting_account_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        AuthRepository auth = new AuthRepository();
        if (auth.isSignedIn()) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.setting_account_signout_title)
                    .setMessage(R.string.setting_account_signout_message)
                    .setNegativeButton(R.string.settings_cancel, null)
                    .setPositiveButton(R.string.settings_sign_out, (dialog, which) -> {
                        new AuthRepository().signOut();
                        refreshRows();
                    })
                    .show();
        } else {
            startActivity(new Intent(context, SignInActivity.class));
        }
    }

    private void showLanguageDialog() {
        Context context = requireContext();
        String[] labels = {
                getString(R.string.settings_lang_system),
                getString(R.string.settings_lang_english),
                getString(R.string.settings_lang_bangla)
        };
        String current = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        int checked = 0;
        if (current != null && current.startsWith("en")) {
            checked = 1;
        } else if (current != null && current.startsWith("bn")) {
            checked = 2;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_language_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    // The app restarts its screens in the new language. It stays inside CivicLens.
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(LANGUAGE_TAGS[which]));
                })
                .setNegativeButton(R.string.settings_cancel, null)
                .show();
    }
}

package com.example.civiclens.ui;

import android.Manifest;
import android.content.IntentFilter;
import androidx.activity.OnBackPressedCallback;
import android.net.ConnectivityManager;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.civiclens.R;
import com.example.civiclens.auth.AuthRepository;
import com.example.civiclens.firebase.FirebaseGate;
import com.example.civiclens.data.AppPrefs;
import com.example.civiclens.data.ProjectRepository;
import com.example.civiclens.data.ServiceRepository;
import com.example.civiclens.receiver.ConnectivityReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/** Hosts the five main screens and listens for connectivity changes. */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Snackbar offlineBar;

    private final ConnectivityReceiver connectivityReceiver =
            new ConnectivityReceiver(this::onConnectivityChanged);

    private final com.example.civiclens.receiver.SystemBroadcastReceiver systemReceiver =
            new com.example.civiclens.receiver.SystemBroadcastReceiver((title, desc) -> { });

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Seeding runs on the shared database worker, so it finishes before any screen reads.
        new ServiceRepository(this).seedIfEmpty();
        new ProjectRepository(this).seedIfEmpty();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.nav_offices) {
                fragment = new OfficesFragment();
            } else if (id == R.id.nav_projects) {
                fragment = new ProjectsFragment();
            } else if (id == R.id.nav_my_services) {
                fragment = new MyServicesFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            } else {
                fragment = new HomeFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        FloatingActionButton fabAsk = findViewById(R.id.fabAsk);
        if (fabAsk != null) {
            fabAsk.setOnClickListener(v -> startActivity(new Intent(this, AskActivity.class)));
        }

        View themeBtn = findViewById(R.id.btnQuickThemeToggle);
        if (themeBtn != null) {
            themeBtn.setOnClickListener(v -> {
                boolean newDark = !AppPrefs.isDarkMode(this);
                AppPrefs.setDarkMode(this, newDark);
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        newDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, newDark ? "🌙 Dark Theme Active" : "☀️ Light Theme Active", false);
            });
        }

        com.example.civiclens.auth.AuthRepository auth = new com.example.civiclens.auth.AuthRepository();
        if (FirebaseGate.isReady(this)) {
            com.example.civiclens.cloud.CloudInvitationRepository.listenForCrossDeviceInvitations(this, auth.currentEmail());
            if (auth.isSignedIn()) {
                com.example.civiclens.cloud.GroupCloudRepository.registerFcmToken(this);
            }
        }

        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_citizens");
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("invitations");
        } catch (Exception ignored) { }

        // Only open sign-in page if Firebase is configured, user is NOT signed in, and prompt hasn't been skipped before.
        if (savedInstanceState == null && FirebaseGate.isReady(this) && !new AuthRepository().isSignedIn() && !AppPrefs.hasSeenSignInPrompt(this)) {
            startActivity(new Intent(this, SignInActivity.class));
        }

        // Back returns to Home first. It closes the app only from Home.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Android 13 and newer must ask before the app can show notifications.
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ContextCompat.registerReceiver(this, connectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
                ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(this, systemReceiver,
                com.example.civiclens.receiver.SystemBroadcastReceiver.createFilter(),
                ContextCompat.RECEIVER_NOT_EXPORTED);
        onConnectivityChanged(ConnectivityReceiver.isOnline(this));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(connectivityReceiver);
        unregisterReceiver(systemReceiver);
        super.onStop();
    }

    /** Lets fragments switch tabs, for example from the quick action cards on Home. */
    public void selectTab(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }

    private void onConnectivityChanged(boolean online) {
        View root = findViewById(R.id.root);
        if (!online) {
            if (offlineBar == null) {
                offlineBar = Snackbar.make(root, R.string.offline_banner, Snackbar.LENGTH_INDEFINITE);
            }
            offlineBar.show();
        } else if (offlineBar != null) {
            offlineBar.dismiss();
            offlineBar = null;
            Snackbar.make(root, R.string.back_online, Snackbar.LENGTH_SHORT).show();
        }
    }
}

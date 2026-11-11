package com.example.civiclens.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.civiclens.R;
import com.example.civiclens.auth.AuthRepository;
import com.example.civiclens.cloud.UserProfileRepository;
import com.example.civiclens.firebase.FirebaseGate;

/** Email and password sign-in or sign-up. Saves the new user's profile to Firestore. */
public class SignInActivity extends AppCompatActivity {

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private TextView statusText;

    private final AuthRepository auth = new AuthRepository();
    private final UserProfileRepository profiles = new UserProfileRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (!FirebaseGate.isReady(this)) {
            Toast.makeText(this, R.string.firebase_not_configured, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        statusText = findViewById(R.id.authStatus);

        findViewById(R.id.signInButton).setOnClickListener(v -> submit(false));
        findViewById(R.id.signUpButton).setOnClickListener(v -> submit(true));
        findViewById(R.id.skipButton).setOnClickListener(v -> {
            com.example.civiclens.data.AppPrefs.markSignInPromptSeen(this);
            finish();
        });
    }

    private void submit(boolean createAccount) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || password.length() < 6) {
            statusText.setText(R.string.auth_need_email_password);
            return;
        }
        statusText.setText(R.string.auth_working);

        AuthRepository.Callback done = (ok, message) -> {
            if (!ok) {
                statusText.setText(message);
                return;
            }
            if (createAccount) {
                profiles.save(auth.currentUid(), name, email);
            }
            com.example.civiclens.cloud.GroupCloudRepository.registerFcmToken(this);
            finish();
        };

        if (createAccount) {
            auth.signUp(email, password, done);
        } else {
            auth.signIn(email, password, done);
        }
    }
}

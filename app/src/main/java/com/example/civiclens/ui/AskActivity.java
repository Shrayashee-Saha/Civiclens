package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.R;
import com.example.civiclens.ai.CatalogEntry;
import com.example.civiclens.ai.PromptBuilder;
import com.example.civiclens.assistant.CatalogSnapshot;
import com.example.civiclens.assistant.GeminiProxyClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ask CivicLens. Gemini answers only from verified records. If the proxy is not configured or
 * fails, the screen shows those same records directly, so the user still gets useful information.
 */
public class AskActivity extends AppCompatActivity {

    private static final ExecutorService NETWORK = Executors.newSingleThreadExecutor();

    private TextInputEditText questionInput;
    private MaterialButton askButton;
    private ProgressBar progress;
    private TextView answerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        questionInput = findViewById(R.id.questionInput);
        askButton = findViewById(R.id.askButton);
        progress = findViewById(R.id.progressBar);
        answerText = findViewById(R.id.answerText);

        askButton.setOnClickListener(v -> onAsk());

        MaterialButton readAloudBtn = findViewById(R.id.readAloudButton);
        if (readAloudBtn != null) {
            readAloudBtn.setOnClickListener(v -> {
                com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
                com.example.civiclens.accessibility.AccessibilityHelper.speak(this, answerText.getText().toString());
            });
        }
    }

    private void onAsk() {
        String question = questionInput.getText() == null ? "" : questionInput.getText().toString().trim();
        if (question.isEmpty()) {
            answerText.setText(R.string.ask_empty_error);
            return;
        }
        setBusy(true);

        CatalogSnapshot.load(this, question, 3, entries -> {
            String language = containsBangla(question) ? "Bangla" : "English";
            String prompt = PromptBuilder.build(question, entries, language);
            String apiKey = BuildConfig.GEMINI_API_KEY;
            String url = BuildConfig.ASSISTANT_URL;

            NETWORK.execute(() -> {
                try {
                    String reply;
                    if (apiKey != null && !apiKey.isEmpty()) {
                        reply = GeminiProxyClient.askDirect(apiKey, prompt);
                    } else if (url != null && !url.isEmpty()) {
                        reply = GeminiProxyClient.ask(url, prompt);
                    } else {
                        runOnUiThread(() -> showLocalAnswer(entries, R.string.assistant_not_configured));
                        return;
                    }
                    runOnUiThread(() -> showAnswer(reply));
                } catch (Exception e) {
                    runOnUiThread(() -> showLocalAnswer(entries, R.string.assistant_unavailable));
                }
            });
        });
    }

    private void showAnswer(String reply) {
        setBusy(false);
        answerText.setText(reply + "\n\n" + getString(R.string.ask_disclaimer));
        com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
        com.example.civiclens.accessibility.AccessibilityHelper.speak(this, reply);
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }

    /** Shows the verified records directly, without any AI, as the fallback. */
    private void showLocalAnswer(List<CatalogEntry> entries, int reasonRes) {
        setBusy(false);
        if (entries.isEmpty()) {
            answerText.setText(getString(reasonRes) + "\n\n" + getString(R.string.assistant_no_match));
            return;
        }
        StringBuilder sb = new StringBuilder(getString(reasonRes)).append("\n");
        for (CatalogEntry e : entries) {
            sb.append("\n").append(e.name).append(" (").append(e.authority).append(")\n");
            sb.append(getString(R.string.assistant_status_label)).append(": ")
              .append(e.demo ? getString(R.string.assistant_demo_label) : e.lastVerified).append("\n");
            if (!e.mandatoryDocuments.isEmpty()) {
                sb.append(getString(R.string.assistant_documents_label)).append(": ")
                  .append(android.text.TextUtils.join("; ", e.mandatoryDocuments)).append("\n");
            }
            sb.append(getString(R.string.assistant_source_format, e.sourceUrl)).append("\n");
        }
        sb.append("\n").append(getString(R.string.ask_disclaimer));
        answerText.setText(sb.toString());
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        askButton.setEnabled(!busy);
    }

    private static boolean containsBangla(String text) {
        for (char c : text.toCharArray()) {
            if (c >= '\u0980' && c <= '\u09FF') {
                return true;
            }
        }
        return false;
    }
}

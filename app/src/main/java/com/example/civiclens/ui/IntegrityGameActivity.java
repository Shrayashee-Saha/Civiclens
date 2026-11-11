package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import com.example.civiclens.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Civic Integrity Quest (2D Interactive Scenario Decision Game for Kids)
 */
public class IntegrityGameActivity extends AppCompatActivity {

    private static class Scenario {
        final String step;
        final String question;
        final String choiceA;
        final String choiceB;
        final boolean isAOptionHonest;
        final int points;

        Scenario(String step, String question, String choiceA, String choiceB, boolean isAOptionHonest, int points) {
            this.step = step;
            this.question = question;
            this.choiceA = choiceA;
            this.choiceB = choiceB;
            this.isAOptionHonest = isAOptionHonest;
            this.points = points;
        }
    }

    private final List<Scenario> scenarios = new ArrayList<>();
    private int currentScenarioIndex = 0;
    private int totalScore = 0;

    private TextView scoreText;
    private TextView badgeText;
    private TextView stepText;
    private TextView questionText;
    private MaterialButton btnA;
    private MaterialButton btnB;
    private View scenarioCard;
    private View victoryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integrity_game);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scoreText = findViewById(R.id.gameScoreText);
        badgeText = findViewById(R.id.gamePlayerBadge);
        stepText = findViewById(R.id.scenarioStepText);
        questionText = findViewById(R.id.scenarioQuestionText);
        btnA = findViewById(R.id.btnChoiceA);
        btnB = findViewById(R.id.btnChoiceB);
        scenarioCard = findViewById(R.id.scenarioCard);
        victoryCard = findViewById(R.id.victoryCard);

        findViewById(R.id.btnRestartGame).setOnClickListener(v -> restartGame());

        btnA.setOnClickListener(v -> onChoiceSelected(true));
        btnB.setOnClickListener(v -> onChoiceSelected(false));

        initScenarios();
        renderCurrentScenario();
    }

    private void initScenarios() {
        scenarios.clear();
        scenarios.add(new Scenario(
                "Scenario 1 of 5",
                "You find a lost wallet near school with money and an NID card inside. What do you do?",
                "Choice A: Return it to DC Office / Civic Help Desk (+100 Points)",
                "Choice B: Keep the money (0 Points)",
                true, 100
        ));
        scenarios.add(new Scenario(
                "Scenario 2 of 5",
                "Someone asks you to throw plastic trash into the local river. What do you do?",
                "Choice A: Use the public recycling bin & protect nature (+100 Points)",
                "Choice B: Throw it into the river (0 Points)",
                true, 100
        ));
        scenarios.add(new Scenario(
                "Scenario 3 of 5",
                "You witness someone damaging a street light or public bench. What do you do?",
                "Choice A: File a report via CivicLens Report Hub (+100 Points)",
                "Choice B: Walk away and ignore it (0 Points)",
                true, 100
        ));
        scenarios.add(new Scenario(
                "Scenario 4 of 5",
                "A friend offers extra money to jump the line for a public service. What do you do?",
                "Choice A: Stand in line patiently and follow rules (+100 Points)",
                "Choice B: Pay extra to jump the line (0 Points)",
                true, 100
        ));
        scenarios.add(new Scenario(
                "Scenario 5 of 5",
                "An elderly citizen needs help crossing a busy road near a school. What do you do?",
                "Choice A: Help them cross safely (+100 Points)",
                "Choice B: Walk past without helping (0 Points)",
                true, 100
        ));
    }

    private void renderCurrentScenario() {
        if (currentScenarioIndex >= scenarios.size()) {
            showVictory();
            return;
        }

        Scenario s = scenarios.get(currentScenarioIndex);
        stepText.setText(s.step);
        questionText.setText(s.question);
        btnA.setText(s.choiceA);
        btnB.setText(s.choiceB);

        scoreText.setText("🏆 Integrity Score: " + totalScore + " Points");
        if (totalScore >= 300) {
            badgeText.setText("⭐ Level 3: Senior Integrity Ambassador");
        } else if (totalScore >= 100) {
            badgeText.setText("⭐ Level 2: Active Civic Guardian");
        } else {
            badgeText.setText("⭐ Level 1: Junior Citizen Trainee");
        }
    }

    private void onChoiceSelected(boolean isChoiceA) {
        Scenario s = scenarios.get(currentScenarioIndex);
        boolean isHonest = isChoiceA == s.isAOptionHonest;

        if (isHonest) {
            totalScore += s.points;
            com.example.civiclens.accessibility.AccessibilityHelper.vibrateSuccess(this);
            Toast.makeText(this, "🌟 Excellent Integrity Choice! +" + s.points + " Points", Toast.LENGTH_SHORT).show();
        } else {
            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "⚠️ Honesty & Accountability are key civic values!", false);
        }

        currentScenarioIndex++;
        renderCurrentScenario();
    }

    private void showVictory() {
        scenarioCard.setVisibility(View.GONE);
        victoryCard.setVisibility(View.VISIBLE);
        scoreText.setText("🏆 Final Integrity Score: " + totalScore + " / 500 Points");
        badgeText.setText("⭐ EARNED: Junior Civic Integrity Ambassador");
        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "🎉 Congratulations! You earned the Junior Civic Integrity Ambassador Badge!", false);
    }

    private void restartGame() {
        currentScenarioIndex = 0;
        totalScore = 0;
        scenarioCard.setVisibility(View.VISIBLE);
        victoryCard.setVisibility(View.GONE);
        renderCurrentScenario();
    }

    @Override
    protected void onDestroy() {
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}

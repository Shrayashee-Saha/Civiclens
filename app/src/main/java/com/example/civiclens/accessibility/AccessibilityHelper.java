package com.example.civiclens.accessibility;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Accessibility, Sound & Haptic Vibration Engine.
 * Provides tactile vibration patterns for Deaf & Hard-of-Hearing users,
 * and Text-To-Speech (TTS) Voice Readouts for visually impaired users.
 */
public final class AccessibilityHelper {

    private static TextToSpeech tts;
    private static boolean ttsInitialized = false;

    private AccessibilityHelper() { }

    /** Tactile Vibration Feedback for Deaf / Hard-of-Hearing Users (Success) */
    public static void vibrateSuccess(Context context) {
        vibratePattern(context, new long[]{0, 80, 50, 80});
    }

    /** Tactile Vibration Feedback for Deaf / Hard-of-Hearing Users (Warning / Alert) */
    public static void vibrateWarning(Context context) {
        vibratePattern(context, new long[]{0, 200, 100, 200, 100, 300});
    }

    private static void vibratePattern(Context context, long[] pattern) {
        if (context == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager manager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (manager != null) {
                    Vibrator vibrator = manager.getDefaultVibrator();
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                }
            } else {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    } else {
                        vibrator.vibrate(pattern, -1);
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    /** Text-To-Speech (TTS) Voice Readout for Accessibility */
    public static void speak(Context context, String text) {
        if (context == null || text == null || text.trim().isEmpty()) return;
        Context app = context.getApplicationContext();

        if (tts == null) {
            tts = new TextToSpeech(app, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsInitialized = true;
                    tts.setLanguage(Locale.US);
                    speakTextInternal(text);
                }
            });
        } else if (ttsInitialized) {
            speakTextInternal(text);
        }
    }

    @SuppressWarnings("deprecation")
    private static void speakTextInternal(String text) {
        if (tts != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "civiclens_tts");
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public static void stop() {
        if (tts != null) {
            try {
                tts.stop();
            } catch (Exception ignored) { }
        }
    }

    /** Notifies the user with both Toast and Tactile Haptic Vibration */
    public static void notifyWithHaptic(Context context, String message, boolean isWarning) {
        if (context == null) return;
        Toast.makeText(context, message, isWarning ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        if (isWarning) {
            vibrateWarning(context);
        } else {
            vibrateSuccess(context);
        }
    }
}

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

    /** Exports official documents (RTI Applications, Complaints) to PDF with styled formatting */
    public static void exportToPdf(Context context, String title, String content) {
        if (context == null || content == null || content.isEmpty()) return;
        try {
            android.graphics.pdf.PdfDocument pdfDocument = new android.graphics.pdf.PdfDocument();
            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
            android.graphics.pdf.PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            android.graphics.Canvas canvas = page.getCanvas();

            // 1. Draw Navy Header Banner
            android.graphics.Paint headerPaint = new android.graphics.Paint();
            headerPaint.setColor(android.graphics.Color.parseColor("#0F172A"));
            canvas.drawRect(0, 0, 595, 80, headerPaint);

            // 2. Draw Header Text
            android.graphics.Paint headerTitlePaint = new android.graphics.Paint();
            headerTitlePaint.setColor(android.graphics.Color.WHITE);
            headerTitlePaint.setTextSize(15f);
            headerTitlePaint.setFakeBoldText(true);
            canvas.drawText("PEOPLE'S REPUBLIC OF BANGLADESH — RTI FORM 'KA'", 30, 42, headerTitlePaint);

            android.graphics.Paint subHeaderPaint = new android.graphics.Paint();
            subHeaderPaint.setColor(android.graphics.Color.parseColor("#10B981"));
            subHeaderPaint.setTextSize(10f);
            canvas.drawText("RIGHT TO INFORMATION ACT 2009 — OFFICIAL LEGAL APPLICATION", 30, 62, subHeaderPaint);

            // 3. Draw Outer Border Box
            android.graphics.Paint borderPaint = new android.graphics.Paint();
            borderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            borderPaint.setStrokeWidth(1.5f);
            borderPaint.setColor(android.graphics.Color.parseColor("#CBD5E1"));
            canvas.drawRect(20, 95, 575, 810, borderPaint);

            // 4. Draw Document Text Content
            android.graphics.Paint textPaint = new android.graphics.Paint();
            textPaint.setTextSize(11f);
            textPaint.setColor(android.graphics.Color.parseColor("#0F172A"));

            String[] lines = content.split("\n");
            int y = 125;
            for (String line : lines) {
                if (y > 720) break;
                canvas.drawText(line, 40, y, textPaint);
                y += 18;
            }

            // 5. Draw Official Signature Box
            canvas.drawLine(380, 760, 550, 760, borderPaint);
            android.graphics.Paint sigPaint = new android.graphics.Paint();
            sigPaint.setTextSize(10f);
            sigPaint.setColor(android.graphics.Color.GRAY);
            canvas.drawText("Signature of Applicant & Date", 390, 775, sigPaint);

            pdfDocument.finishPage(page);

            java.io.File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();
            java.io.File pdfFile = new java.io.File(downloadsDir, title.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            vibrateSuccess(context);
            Toast.makeText(context, "📄 Official PDF Saved: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
            com.example.civiclens.ui.IntentHelper.openPdfFile(context, pdfFile);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to export PDF: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

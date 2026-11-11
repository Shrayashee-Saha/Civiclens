package com.example.civiclens.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.civiclens.R;
import com.example.civiclens.data.AppPrefs;
import com.example.civiclens.model.GovernmentOffice;

/**
 * Every outside app the app hands off to, in one place. Each call opens another app
 * (dialer, messages, email, gallery, map, browser) and never sends anything silently.
 */
public final class IntentHelper {

    public static final String FIREFOX_PACKAGE = "org.mozilla.firefox";

    private IntentHelper() { }

    /** Opens the phone's dialer with the number filled in. The user presses call. */
    public static void dial(Context context, String phone) {
        if (TextUtils.isEmpty(phone)) {
            return;
        }
        start(context, new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
    }

    /** Opens the messaging app with the number and optional text filled in. */
    public static void sms(Context context, String phone, String body) {
        if (TextUtils.isEmpty(phone)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
        if (body != null) {
            intent.putExtra("sms_body", body);
        }
        start(context, intent);
    }

    /** Opens the email app. If a photo is given, the photo is attached through the gallery share. */
    public static void email(Context context, String address, String subject, String body, Uri photo) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        Intent intent;
        if (photo != null) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, photo);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        }
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        start(context, intent);
    }

    /** Opens the system share sheet with plain text, so the user picks any app. */
    public static void shareText(Context context, String text, String chooserTitle) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        start(context, Intent.createChooser(send, chooserTitle));
    }

    /** Opens a map search for the office by name and district. Coordinates are not invented. */
    public static void openMap(Context context, GovernmentOffice office) {
        String query = office.nameEn + ", " + office.district + ", Bangladesh";
        start(context, new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))));
    }

    /**
     * Opens a web page. If the user chose Firefox in Settings and it is installed, Firefox opens
     * the page. Otherwise the system picks a browser.
     */
    public static void openUrl(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(context, "No news URL available.", Toast.LENGTH_SHORT).show();
            return;
        }
        String cleanUrl = url.trim().replace("&amp;", "&").replaceAll("\\s+", "");
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://" + cleanUrl;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_app_for_action, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void openPdfFile(Context context, java.io.File pdfFile) {
        if (context == null || pdfFile == null || !pdfFile.exists()) return;
        try {
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    context, context.getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            shareText(context, "RTI Document generated: " + pdfFile.getAbsolutePath(), "Share PDF Document");
        }
    }

    private static void start(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_app_for_action, Toast.LENGTH_SHORT).show();
        }
    }
}

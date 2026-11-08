package com.example.civiclens.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Small user choices that are not data records. */
public final class AppPrefs {

    private static final String PREFS = "civiclens_prefs";
    private static final String KEY_FIREFOX = "prefer_firefox";
    private static final String KEY_SIGNIN_PROMPT_SEEN = "signin_prompt_seen";

    private AppPrefs() { }

    public static boolean preferFirefox(Context context) {
        return prefs(context).getBoolean(KEY_FIREFOX, false);
    }

    public static void setPreferFirefox(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_FIREFOX, value).apply();
    }

    public static boolean hasSeenSignInPrompt(Context context) {
        return prefs(context).getBoolean(KEY_SIGNIN_PROMPT_SEEN, false);
    }

    public static void markSignInPromptSeen(Context context) {
        prefs(context).edit().putBoolean(KEY_SIGNIN_PROMPT_SEEN, true).apply();
    }

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean("dark_mode_enabled", false);
    }

    public static void setDarkMode(Context context, boolean value) {
        prefs(context).edit().putBoolean("dark_mode_enabled", value).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

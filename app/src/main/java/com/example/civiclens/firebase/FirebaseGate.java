package com.example.civiclens.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;

/**
 * Firebase starts only when app/google-services.json exists. Check this before touching any
 * Firebase class, or the app crashes in builds that have no Firebase project.
 */
public final class FirebaseGate {

    private FirebaseGate() { }

    public static boolean isReady(Context context) {
        return !FirebaseApp.getApps(context).isEmpty();
    }
}

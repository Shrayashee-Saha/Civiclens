package com.example.civiclens.auth;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Firebase Authentication with email and password.
 * Call only after FirebaseGate.isReady(context) returns true.
 */
public class AuthRepository {

    public interface Callback {
        /** Called on the main thread. message is null on success. */
        void onResult(boolean success, String message);
    }

    public boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? null : user.getUid();
    }

    public String currentEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? null : user.getEmail();
    }

    public void signUp(String email, String password, Callback callback) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> callback.onResult(task.isSuccessful(), describe(task)));
    }

    public void signIn(String email, String password, Callback callback) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> callback.onResult(task.isSuccessful(), describe(task)));
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private static String describe(Task<?> task) {
        if (task.isSuccessful()) {
            return null;
        }
        Exception e = task.getException();
        return e == null ? "Something went wrong. Please try again." : e.getLocalizedMessage();
    }
}

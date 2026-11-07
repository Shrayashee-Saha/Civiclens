package com.example.civiclens.cloud;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Firestore CRUD for each user's profile, stored at users/{uid}.
 * Call only after FirebaseGate.isReady(context) returns true.
 */
public class UserProfileRepository {

    public interface ProfileCallback {
        /** Null when the profile does not exist. */
        void onProfile(Map<String, Object> profile);
    }

    // CREATE or UPDATE. merge keeps any fields this method does not send.
    public void save(String uid, String displayName, String email) {
        Map<String, Object> data = new HashMap<>();
        data.put("displayName", displayName);
        data.put("email", email);
        data.put("updatedAt", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(data, SetOptions.merge());
    }

    // READ
    public void load(String uid, ProfileCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> callback.onProfile(doc.exists() ? doc.getData() : null));
    }

    // DELETE
    public void delete(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).delete();
    }
}

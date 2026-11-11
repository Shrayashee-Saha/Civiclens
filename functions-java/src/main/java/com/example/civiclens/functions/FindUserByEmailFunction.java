package com.example.civiclens.functions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Java Cloud Function: find-user-by-email
 * Finds a user by email returning ONLY uid and displayName. Never returns email address.
 */
public class FindUserByEmailFunction extends CallableHandler {

    @Override
    protected Map<String, Object> handleCall(String callerUid, String callerEmail, JsonObject data) throws Exception {
        String email = data.has("email") ? data.get("email").getAsString().trim() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
        if (userRecord == null) {
            throw new IllegalArgumentException("No user found with that email.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("uid", userRecord.getUid());
        response.put("displayName", userRecord.getDisplayName() != null ? userRecord.getDisplayName() : "Civic Member");
        return response;
    }
}

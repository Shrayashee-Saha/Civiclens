package com.example.civiclens.functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared base class for Firebase Callable HTTP Cloud Functions in Java.
 * Verifies Firebase Auth ID Token from "Authorization: Bearer <token>" header
 * and structures responses in the standard Firebase Callable Protocol format.
 */
public abstract class CallableHandler implements HttpFunction {

    protected static final Gson GSON = new Gson();

    static {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(FirebaseOptions.builder().build());
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        response.appendHeader("Content-Type", "application/json; charset=utf-8");
        BufferedWriter writer = response.getWriter();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.appendHeader("Access-Control-Allow-Origin", "*");
            response.appendHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setStatusCode(204);
            return;
        }

        response.appendHeader("Access-Control-Allow-Origin", "*");

        String authHeader = request.getFirstHeader("Authorization").orElse("");
        if (!authHeader.startsWith("Bearer ")) {
            sendError(writer, 401, "UNAUTHENTICATED", "Missing or invalid authorization token.");
            return;
        }

        String idToken = authHeader.substring(7).trim();
        FirebaseToken decodedToken;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            sendError(writer, 401, "UNAUTHENTICATED", "Invalid authentication token: " + e.getMessage());
            return;
        }

        JsonObject body;
        try {
            body = GSON.fromJson(new InputStreamReader(request.getInputStream()), JsonObject.class);
        } catch (Exception e) {
            sendError(writer, 400, "INVALID_ARGUMENT", "Invalid JSON body.");
            return;
        }

        JsonObject data = (body != null && body.has("data")) ? body.getAsJsonObject("data") : new JsonObject();

        try {
            Map<String, Object> result = handleCall(decodedToken.getUid(), decodedToken.getEmail(), data);
            sendResult(writer, result);
        } catch (IllegalArgumentException e) {
            sendError(writer, 400, "INVALID_ARGUMENT", e.getMessage());
        } catch (IllegalStateException e) {
            sendError(writer, 403, "PERMISSION_DENIED", e.getMessage());
        } catch (Exception e) {
            sendError(writer, 500, "INTERNAL", e.getMessage());
        }
    }

    protected abstract Map<String, Object> handleCall(String uid, String email, JsonObject data) throws Exception;

    private void sendResult(BufferedWriter writer, Map<String, Object> result) throws Exception {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("result", result != null ? result : new HashMap<>());
        writer.write(GSON.toJson(wrapper));
    }

    private void sendError(BufferedWriter writer, int httpCode, String status, String message) throws Exception {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status);
        errorDetails.put("message", message);

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("error", errorDetails);
        writer.write(GSON.toJson(wrapper));
    }
}

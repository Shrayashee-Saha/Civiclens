package com.example.civiclens.functions;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Java Background Cloud Function Trigger: notify-invitation-created
 * Triggers on Firestore document creation in invitations/{invitationId}.
 * Reads the receiver's fcmToken from users/{receiverId} and dispatches FCM push notification.
 */
public class NotifyInvitationCreatedFunction implements BackgroundFunction<NotifyInvitationCreatedFunction.FirestoreEvent> {

    private static final Gson GSON = new Gson();

    static {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(FirebaseOptions.builder().build());
        }
    }

    public static class FirestoreEvent {
        public JsonObject value;
    }

    @Override
    public void accept(FirestoreEvent event, Context context) throws Exception {
        if (event == null || event.value == null || !event.value.has("fields")) return;

        JsonObject fields = event.value.getAsJsonObject("fields");
        String status = getStringField(fields, "status");
        if (!"PENDING".equalsIgnoreCase(status)) return;

        String receiverId = getStringField(fields, "receiverId");
        String groupName = getStringField(fields, "groupName");
        String senderName = getStringField(fields, "senderName");
        String groupId = getStringField(fields, "groupId");

        if (receiverId.isEmpty()) return;

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot userDoc = db.collection("users").document(receiverId).get().get();

        if (!userDoc.exists() || !userDoc.contains("fcmToken")) return;
        String fcmToken = userDoc.getString("fcmToken");
        if (fcmToken == null || fcmToken.trim().isEmpty()) return;

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("📩 Group Invitation")
                        .setBody((senderName.isEmpty() ? "A member" : senderName) + " invited you to join " + (groupName.isEmpty() ? "a civic group" : groupName) + ".")
                        .build())
                .putData("groupId", groupId)
                .putData("groupName", groupName)
                .putData("type", "INVITATION")
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("UNREGISTERED") || e.getMessage().contains("invalid-registration-token"))) {
                Map<String, Object> removeToken = new HashMap<>();
                removeToken.put("fcmToken", null);
                db.collection("users").document(receiverId).set(removeToken, com.google.cloud.firestore.SetOptions.merge());
            }
        }
    }

    private String getStringField(JsonObject fields, String key) {
        if (fields.has(key)) {
            JsonObject fieldObj = fields.getAsJsonObject(key);
            if (fieldObj.has("stringValue")) {
                return fieldObj.get("stringValue").getAsString();
            }
        }
        return "";
    }
}

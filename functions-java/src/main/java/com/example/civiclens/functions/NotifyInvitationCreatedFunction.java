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
        System.out.println("NOTIFY_INVITE_FIRED: " + (event != null ? GSON.toJson(event) : "null"));

        if (event == null || event.value == null) {
            System.out.println("NOTIFY_INVITE_SKIPPED: event or value is null");
            return;
        }

        JsonObject fields = null;
        if (event.value.has("fields")) {
            fields = event.value.getAsJsonObject("fields");
        } else if (event.value.has("value") && event.value.getAsJsonObject("value").has("fields")) {
            fields = event.value.getAsJsonObject("value").getAsJsonObject("fields");
        }

        if (fields == null) {
            System.out.println("NOTIFY_INVITE_SKIPPED: fields structure not found");
            return;
        }

        String status = getStringField(fields, "status");
        if (!"PENDING".equalsIgnoreCase(status)) {
            System.out.println("NOTIFY_INVITE_SKIPPED: status is not PENDING (" + status + ")");
            return;
        }

        String receiverId = getStringField(fields, "receiverId");
        String groupName = getStringField(fields, "groupName");
        String senderName = getStringField(fields, "senderName");
        String groupId = getStringField(fields, "groupId");

        System.out.println("NOTIFY_INVITE_PROCESSING: receiverId=" + receiverId + ", groupName=" + groupName + ", senderName=" + senderName);

        if (receiverId.isEmpty()) return;

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot userDoc = db.collection("users").document(receiverId).get().get();

        if (!userDoc.exists() || !userDoc.contains("fcmToken")) {
            System.out.println("NOTIFY_INVITE_SKIPPED: No fcmToken found in users/" + receiverId);
            return;
        }

        String fcmToken = userDoc.getString("fcmToken");
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            System.out.println("NOTIFY_INVITE_SKIPPED: fcmToken is empty for users/" + receiverId);
            return;
        }

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
            String messageId = FirebaseMessaging.getInstance().send(message);
            System.out.println("NOTIFY_INVITE_SUCCESS: Push dispatched! MessageId=" + messageId);
        } catch (Exception e) {
            System.err.println("NOTIFY_INVITE_FCM_ERROR: " + e.getMessage());
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

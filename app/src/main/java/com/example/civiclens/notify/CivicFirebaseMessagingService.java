package com.example.civiclens.notify;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Listens for FCM push notifications regarding project updates, news alerts, and citizen observations.
 */
public class CivicFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "CivicLens Alert";
        String body = "New project update published.";
        String projectId = null;

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().containsKey("title")) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().containsKey("body")) {
                body = remoteMessage.getData().get("body");
            }
            if (remoteMessage.getData().containsKey("projectId")) {
                projectId = remoteMessage.getData().get("projectId");
            }
        }

        NotificationHelper.showDirectAlert(getApplicationContext(), title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance("https://civiclens-e2000-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("fcm_tokens")
                    .child(token.replaceAll("[^a-zA-Z0-9_-]", "_"))
                    .setValue(token);
        } catch (Exception ignored) { }
    }
}

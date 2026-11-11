package com.example.civiclens.cloud;

import android.content.Context;

import com.example.civiclens.firebase.FirebaseGate;
import com.example.civiclens.notify.NotificationHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Realtime Database Cross-Device Invitation & Sync Engine.
 * Sends invitations to Firebase Cloud under invitations/{cleanEmail}
 * and listens live across all devices to trigger cross-device system notifications!
 */
public class CloudInvitationRepository {

    private static String cleanEmailKey(String email) {
        if (email == null || email.trim().isEmpty()) return "general_citizen";
        return email.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public static final String FIREBASE_DB_URL = "https://civiclens-e2000-default-rtdb.asia-southeast1.firebasedatabase.app";

    /** Sends invitation payload to Firebase Cloud & HTTP Relay to notify target device. */
    public static void sendInvitation(Context context, String senderEmail, String targetEmail, String groupName) {
        if (context == null || targetEmail == null || targetEmail.trim().isEmpty()) return;

        if (FirebaseGate.isReady(context)) {
            try {
                DatabaseReference ref = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("invitations");
                String targetKey = cleanEmailKey(targetEmail);
                String invId = UUID.randomUUID().toString().substring(0, 8);

                Map<String, Object> payload = new HashMap<>();
                payload.put("id", invId);
                payload.put("senderEmail", senderEmail != null ? senderEmail : "Citizen Activist");
                payload.put("targetEmail", targetEmail);
                payload.put("groupName", groupName != null ? groupName : "Civic Assembly Group");
                payload.put("status", "PENDING");
                payload.put("timestamp", System.currentTimeMillis());

                ref.child(targetKey).child(invId).setValue(payload);
                ref.child("global_feed").child(invId).setValue(payload)
                        .addOnSuccessListener(aVoid -> {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    android.widget.Toast.makeText(context, "☁️ Firebase Cloud Sync: Invitation delivered to cloud!", android.widget.Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    android.widget.Toast.makeText(context, "⚠ Firebase Cloud Error: " + e.getLocalizedMessage() + ". Please publish rules in Firebase Console.", android.widget.Toast.LENGTH_LONG).show());
                        });
            } catch (Exception ignored) { }
        }
    }

    private static com.google.firebase.database.ChildEventListener activeChildListener = null;

    /** Listens live on Firebase Cloud for incoming cross-device invitations. */
    public static void listenForCrossDeviceInvitations(Context context, String myEmail) {
        if (context == null || !FirebaseGate.isReady(context)) return;

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("invitations");
            String myKey = cleanEmailKey(myEmail);

            if (activeChildListener != null) {
                try {
                    ref.child("global_feed").removeEventListener(activeChildListener);
                    ref.child(myKey).removeEventListener(activeChildListener);
                } catch (Exception ignored) { }
            }

            final long appLaunchTime = System.currentTimeMillis() - 2000;

            activeChildListener = new com.google.firebase.database.ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                    if (snapshot.exists()) {
                        Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                        if (timestamp != null && timestamp < appLaunchTime) {
                            return; // Filter out historical invitations from earlier app sessions!
                        }

                        String groupName = snapshot.child("groupName").getValue(String.class);
                        String groupId = snapshot.child("groupId").getValue(String.class);
                        String senderEmail = snapshot.child("senderEmail").getValue(String.class);
                        String targetEmail = snapshot.child("targetEmail").getValue(String.class);

                        // 1. Skip if sender is yourself
                        if (myEmail != null && senderEmail != null && senderEmail.trim().equalsIgnoreCase(myEmail.trim())) {
                            return;
                        }

                        // 2. Verify target recipient match
                        boolean isTargetForMe = false;
                        if (myEmail == null || myEmail.trim().isEmpty() || "general_citizen".equals(myKey)) {
                            isTargetForMe = true;
                        } else if (targetEmail != null && (targetEmail.trim().equalsIgnoreCase(myEmail.trim()) || cleanEmailKey(targetEmail).equals(myKey))) {
                            isTargetForMe = true;
                        }

                        if (isTargetForMe && groupName != null) {
                            NotificationHelper.showDirectAlert(
                                    context,
                                    "📩 Group Invitation: " + groupName,
                                    (senderEmail != null ? senderEmail : "Citizen Activist") + " invited you to join " + groupName + " [GroupID:" + (groupId != null ? groupId : "comm_1") + "]"
                            );
                        }
                    }
                }

                @Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) { }
                @Override public void onChildRemoved(DataSnapshot snapshot) { }
                @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) { }
                @Override public void onCancelled(DatabaseError error) { }
            };

            ref.child("global_feed").limitToLast(10).addChildEventListener(activeChildListener);
            ref.child(myKey).limitToLast(10).addChildEventListener(activeChildListener);
        } catch (Exception ignored) { }
    }

    public interface InvitationCallback {
        void onInvitations(List<Map<String, Object>> invitations);
    }

    public static void createFirestoreInvitation(Context context, String senderEmail, String targetEmail, String groupId, String groupName) {
        if (context == null || !FirebaseGate.isReady(context)) return;
        try {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            String invId = UUID.randomUUID().toString();

            Map<String, Object> data = new HashMap<>();
            data.put("id", invId);
            data.put("groupId", groupId != null ? groupId : "group123");
            data.put("groupName", groupName != null ? groupName : "Save Our River");
            data.put("senderEmail", senderEmail != null ? senderEmail : "Citizen Activist");
            data.put("receiverEmail", targetEmail);
            data.put("status", "PENDING");
            data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection("invitations").document(invId).set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid ->
                            android.widget.Toast.makeText(context, "📩 Firestore Invitation Sent to " + targetEmail + "!", android.widget.Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            android.widget.Toast.makeText(context, "⚠ Firestore Error: " + e.getLocalizedMessage(), android.widget.Toast.LENGTH_LONG).show());
        } catch (Exception ignored) { }
    }

    public static void respondToFirestoreInvitation(Context context, String invId, String status, Runnable onDone) {
        if (context == null || !FirebaseGate.isReady(context) || invId == null) return;
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("invitations").document(invId)
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        if ("ACCEPTED".equalsIgnoreCase(status)) {
                            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(context, "✓ Group Invitation Accepted!", false);
                        } else {
                            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(context, "Group Invitation Declined", true);
                        }
                        if (onDone != null) onDone.run();
                    });
        } catch (Exception ignored) { }
    }
}

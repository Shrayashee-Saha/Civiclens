package com.example.civiclens.cloud;

import android.content.Context;

import com.example.civiclens.firebase.FirebaseGate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Group creation, invitations and membership.
 *
 * Every change to membership or invitation status goes through a Cloud Function (see
 * functions-java (see CallableHandler)). Firestore rules block those writes from the app, so this class only
 * calls functions and reads what the user is allowed to read.
 *
 * Call only after FirebaseGate.isReady(context) returns true.
 */
public final class GroupCloudRepository {

    /** Must match REGION in functions-java (see CallableHandler). */
    public static final String REGION = "asia-southeast1";

    public interface Callback {
        /** Called on the main thread. message is null on success. */
        void onResult(boolean success, String message, Map<String, Object> data);
    }

    public interface InvitationListener {
        /** Called on the main thread whenever the user's pending invitations change. */
        void onInvitations(List<PendingInvitation> invitations);
    }

    public static final class PendingInvitation {
        public final String id;
        public final String groupId;
        public final String groupName;
        public final String senderName;

        public PendingInvitation(String id, String groupId, String groupName, String senderName) {
            this.id = id;
            this.groupId = groupId != null ? groupId : "";
            this.groupName = groupName != null ? groupName : "Civic group";
            this.senderName = senderName != null ? senderName : "A CivicLens member";
        }
    }

    private GroupCloudRepository() { }

    /** Creates a group on the server. The caller becomes its owner. */
    public static void createGroup(Context context, String name, String description,
                                   String district, String communityType, Callback callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("description", description);
        payload.put("district", district);
        payload.put("communityType", communityType);
        call(context, "create-group", payload, callback);
    }

    /** Finds a user by email. The result holds uid and displayName only. */
    public static void findUserByEmail(Context context, String email, Callback callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        call(context, "find-user-by-email", payload, callback);
    }

    /** Sends a PENDING invitation. The server checks that the caller is a member. */
    public static void inviteToGroup(Context context, String groupId, String receiverId,
                                     Callback callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("groupId", groupId);
        payload.put("receiverId", receiverId);
        call(context, "invite-to-group", payload, callback);
    }

    /** Accepts or declines an invitation. On accept, the server adds the caller as a member. */
    public static void respondToInvitation(Context context, String invitationId, boolean accept,
                                           Callback callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("invitationId", invitationId);
        payload.put("accept", accept);
        call(context, "respond-to-invitation", payload, callback);
    }

    /**
     * Listens for the signed-in user's PENDING invitations. Keep the returned registration and
     * call remove() when the screen closes. Returns null if the user is not signed in.
     */
    public static ListenerRegistration listenForPendingInvitations(Context context,
                                                                   InvitationListener listener) {
        if (!FirebaseGate.isReady(context)) return null;
        String uid = currentUid();
        if (uid == null) return null;

        return FirebaseFirestore.getInstance()
                .collection("invitations")
                .whereEqualTo("receiverId", uid)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    List<PendingInvitation> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        list.add(new PendingInvitation(
                                doc.getId(),
                                doc.getString("groupId"),
                                doc.getString("groupName"),
                                doc.getString("senderName")));
                    }
                    listener.onInvitations(list);
                });
    }

    /** Saves this device's FCM token to users/{uid} so invitations can be pushed to it. */
    public static void registerFcmToken(Context context) {
        if (!FirebaseGate.isReady(context) || currentUid() == null) return;
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> saveFcmToken(context, token));
    }

    /** Stores a new or rotated FCM token. Does nothing when no user is signed in. */
    public static void saveFcmToken(Context context, String token) {
        if (token == null || !FirebaseGate.isReady(context)) return;
        String uid = currentUid();
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(data, SetOptions.merge());
    }

    private static void call(Context context, String function, Map<String, Object> payload,
                             Callback callback) {
        if (!FirebaseGate.isReady(context) || currentUid() == null) {
            callback.onResult(false, "Sign in to use group invitations.", null);
            return;
        }
        FirebaseFunctions.getInstance(REGION)
                .getHttpsCallable(function)
                .call(payload)
                .addOnSuccessListener(result ->
                        callback.onResult(true, null, asMap(result.getData())))
                .addOnFailureListener(error ->
                        callback.onResult(false, friendlyMessage(error), null));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object data) {
        return data instanceof Map ? (Map<String, Object>) data : new HashMap<>();
    }

    private static String friendlyMessage(Exception error) {
        if (error instanceof FirebaseFunctionsException) {
            String message = error.getMessage();
            if (message != null && !message.isEmpty()) return message;
        }
        return "Something went wrong. Please try again.";
    }

    private static String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? null : user.getUid();
    }
}

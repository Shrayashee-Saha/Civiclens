package com.example.civiclens.functions;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Java Cloud Function: respond-to-invitation
 * Executes inside a Firestore Transaction.
 * On ACCEPT: adds groups/{id}/members/{uid} and increments memberCount once.
 * On DECLINE: sets status to DECLINED.
 */
public class RespondToInvitationFunction extends CallableHandler {

    @Override
    protected Map<String, Object> handleCall(String callerUid, String callerEmail, JsonObject data) throws Exception {
        String invitationId = data.has("invitationId") ? data.get("invitationId").getAsString().trim() : "";
        boolean accept = data.has("accept") && data.get("accept").getAsBoolean();

        if (invitationId.isEmpty()) {
            throw new IllegalArgumentException("invitationId is required.");
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference invRef = db.collection("invitations").document(invitationId);

        db.runTransaction(transaction -> {
            DocumentSnapshot invDoc = transaction.get(invRef).get();
            if (!invDoc.exists()) {
                throw new IllegalArgumentException("Invitation not found.");
            }

            String receiverId = invDoc.getString("receiverId");
            if (!callerUid.equals(receiverId)) {
                throw new IllegalStateException("Only the invited user can respond to this invitation.");
            }

            String status = invDoc.getString("status");
            if (!"PENDING".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Invitation is no longer pending.");
            }

            if (accept) {
                String groupId = invDoc.getString("groupId");
                DocumentReference memberRef = db.collection("groups").document(groupId)
                        .collection("members").document(callerUid);
                DocumentReference groupRef = db.collection("groups").document(groupId);

                Map<String, Object> memberData = new HashMap<>();
                memberData.put("uid", callerUid);
                memberData.put("role", "MEMBER");
                memberData.put("joinedAt", FieldValue.serverTimestamp());

                transaction.set(memberRef, memberData);
                transaction.update(groupRef, "memberCount", FieldValue.increment(1));
                transaction.update(invRef, "status", "ACCEPTED");
            } else {
                transaction.update(invRef, "status", "DECLINED");
            }

            return null;
        }).get();

        Map<String, Object> response = new HashMap<>();
        response.put("invitationId", invitationId);
        response.put("status", accept ? "ACCEPTED" : "DECLINED");
        return response;
    }
}

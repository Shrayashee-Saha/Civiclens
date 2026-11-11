package com.example.civiclens.functions;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Java Cloud Function: invite-to-group
 * Caller must be a member; receiver must exist and not already be a member; no duplicate PENDING invitation.
 * Creates invitations/{id} with status PENDING.
 */
public class InviteToGroupFunction extends CallableHandler {

    @Override
    protected Map<String, Object> handleCall(String callerUid, String callerEmail, JsonObject data) throws Exception {
        String groupId = data.has("groupId") ? data.get("groupId").getAsString().trim() : "";
        String receiverId = data.has("receiverId") ? data.get("receiverId").getAsString().trim() : "";

        if (groupId.isEmpty() || receiverId.isEmpty()) {
            throw new IllegalArgumentException("groupId and receiverId are required.");
        }

        Firestore db = FirestoreClient.getFirestore();

        // 1. Verify caller is a member
        DocumentSnapshot callerMemberDoc = db.collection("groups").document(groupId)
                .collection("members").document(callerUid).get().get();
        if (!callerMemberDoc.exists()) {
            throw new IllegalStateException("Only group members can invite others.");
        }

        // 2. Verify receiver is not already a member
        DocumentSnapshot receiverMemberDoc = db.collection("groups").document(groupId)
                .collection("members").document(receiverId).get().get();
        if (receiverMemberDoc.exists()) {
            throw new IllegalArgumentException("User is already a member of this group.");
        }

        // 3. Verify no duplicate PENDING invitation exists
        QuerySnapshot pendingDoc = db.collection("invitations")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("status", "PENDING")
                .get().get();
        if (!pendingDoc.isEmpty()) {
            throw new IllegalArgumentException("A pending invitation already exists for this user.");
        }

        // 4. Fetch group details
        DocumentSnapshot groupDoc = db.collection("groups").document(groupId).get().get();
        String groupName = groupDoc.exists() && groupDoc.contains("name") ? groupDoc.getString("name") : "Civic group";

        // 5. Create invitation document
        String invitationId = UUID.randomUUID().toString();
        Map<String, Object> invData = new HashMap<>();
        invData.put("id", invitationId);
        invData.put("groupId", groupId);
        invData.put("groupName", groupName);
        invData.put("senderId", callerUid);
        invData.put("senderName", callerEmail != null ? callerEmail : "A CivicLens member");
        invData.put("receiverId", receiverId);
        invData.put("status", "PENDING");
        invData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("invitations").document(invitationId).set(invData).get();

        Map<String, Object> response = new HashMap<>();
        response.put("invitationId", invitationId);
        response.put("status", "PENDING");
        return response;
    }
}

package com.example.civiclens.functions;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Java Cloud Function: create-group
 * Creates a group document in Firestore groups/{id} and adds caller as owner & first member in groups/{id}/members/{uid}.
 */
public class CreateGroupFunction extends CallableHandler {

    @Override
    protected Map<String, Object> handleCall(String uid, String email, JsonObject data) throws Exception {
        String name = data.has("name") ? data.get("name").getAsString().trim() : "";
        String description = data.has("description") ? data.get("description").getAsString().trim() : "";
        String district = data.has("district") ? data.get("district").getAsString().trim() : "Dhaka";
        String communityType = data.has("communityType") ? data.get("communityType").getAsString().trim() : "Public Community";

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Group name is required.");
        }

        String groupId = UUID.randomUUID().toString();
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("id", groupId);
        groupData.put("name", name);
        groupData.put("description", description);
        groupData.put("district", district);
        groupData.put("communityType", communityType);
        groupData.put("ownerId", uid);
        groupData.put("memberCount", 1);
        groupData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("groups").document(groupId).set(groupData).get();

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("uid", uid);
        memberData.put("role", "OWNER");
        memberData.put("joinedAt", FieldValue.serverTimestamp());

        db.collection("groups").document(groupId).collection("members").document(uid).set(memberData).get();

        Map<String, Object> response = new HashMap<>();
        response.put("groupId", groupId);
        response.put("name", name);
        return response;
    }
}

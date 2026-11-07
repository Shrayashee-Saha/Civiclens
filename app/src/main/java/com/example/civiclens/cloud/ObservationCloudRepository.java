package com.example.civiclens.cloud;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Realtime Database CRUD for citizen observations.
 * Data is stored at observations/{projectId}/{observationId}.
 * Call only after FirebaseGate.isReady(context) returns true.
 */
public class ObservationCloudRepository {

    public interface ListCallback {
        void onObservations(List<Map<String, Object>> observations);
    }

    private final DatabaseReference observations =
            FirebaseDatabase.getInstance().getReference("observations");

    /** CREATE. Uses the same id as the local Room row so the two copies match. */
    public Task<Void> create(String id, String projectId, String text, long createdAt, String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("projectId", projectId);
        data.put("text", text);
        data.put("createdAt", createdAt);
        data.put("status", "PENDING_REVIEW");
        data.put("uid", uid);
        return observations.child(projectId).child(id).setValue(data);
    }

    /** READ once. */
    public void readForProject(String projectId, ListCallback callback) {
        observations.child(projectId).get()
                .addOnSuccessListener(snapshot -> callback.onObservations(toList(snapshot)));
    }

    /** READ live. Keep the returned listener and pass it to stopListening(). */
    public ValueEventListener listenForProject(String projectId, ListCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                callback.onObservations(toList(snapshot));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Permission denied or no network. Local data is still shown.
            }
        };
        observations.child(projectId).addValueEventListener(listener);
        return listener;
    }

    public void stopListening(String projectId, ValueEventListener listener) {
        observations.child(projectId).removeEventListener(listener);
    }

    /** UPDATE one field. */
    public Task<Void> updateStatus(String projectId, String id, String status) {
        return observations.child(projectId).child(id).child("status").setValue(status);
    }

    /** DELETE. */
    public Task<Void> delete(String projectId, String id) {
        return observations.child(projectId).child(id).removeValue();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> toList(DataSnapshot snapshot) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Object value = child.getValue();
            if (value instanceof Map) {
                out.add((Map<String, Object>) value);
            }
        }
        return out;
    }
}

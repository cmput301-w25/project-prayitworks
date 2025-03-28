package com.example.moodster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodEventViewModel extends ViewModel {

    private static MoodEventViewModel instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // Local HashMap for offline usage
    private final HashMap<Integer, MoodEvent> moodEvents = new HashMap<>();

    // (Optional) For checking connectivity
    private Context context;

    // For generating local IDs if needed
    private int nextLocalId = 1;

    private MoodEventViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static MoodEventViewModel getInstance() {
        if (instance == null) {
            instance = new MoodEventViewModel();
        }
        return instance;
    }

    // Set context if you want to do connectivity checks
    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    private boolean isOnline() {
        if (context == null) return true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    /**
     * For retrieving existing moods:
     *  1) If user logged in & online => load from Firestore.
     *  2) If offline => fallback to local HashMap.
     */
    public void fetchCurrentUserMoods(OnMoodsFetchedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w("MoodEventViewModel", "No user logged in => returning local data");
            listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
            return;
        }

        String email = currentUser.getEmail();
        if (!isOnline()) {
            Log.d("MoodEventViewModel", "Offline => returning local moods");
            listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
            return;
        }

        // STEP 1: Get username from Firestore
        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String username = userDoc.getString("username");

                        List<Long> moodIds = (List<Long>) userDoc.get("MoodEventIds");
                        if (moodIds == null || moodIds.isEmpty()) {
                            moodEvents.clear();
                            listener.onMoodsFetched(new ArrayList<>());
                            return;
                        }

                        // STEP 2: Fetch all MoodEvents by ID
                        List<MoodEvent> fetched = new ArrayList<>();
                        final int totalToFetch = moodIds.size();

                        for (Long id : moodIds) {
                            db.collection("MoodEvents").document(String.valueOf(id))
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            try {
                                                int docId = doc.getLong("id").intValue();
                                                Timestamp createdAt = doc.getTimestamp("createdAt");
                                                String emoState = doc.getString("emotionalState");
                                                String trigger = doc.getString("trigger");
                                                String social = doc.getString("socialSituation");
                                                String explanation = doc.getString("explanation");
                                                double lat = doc.contains("latitude") ? doc.getDouble("latitude") : 0.0;
                                                double lon = doc.contains("longitude") ? doc.getDouble("longitude") : 0.0;

                                                MoodEvent moodEvent = new MoodEvent(
                                                        docId,
                                                        createdAt,
                                                        emoState,
                                                        trigger,
                                                        social,
                                                        explanation,
                                                        null,
                                                        lat,
                                                        lon
                                                );
                                                fetched.add(moodEvent);
                                            } catch (Exception e) {
                                                Log.w("FetchMoodError", "Error parsing mood document", e);
                                            }
                                        }

                                        if (fetched.size() + 1 >= totalToFetch) {
                                            // Finalize and return
                                            moodEvents.clear();
                                            for (MoodEvent m : fetched) {
                                                moodEvents.put(m.getId(), m);
                                            }
                                            listener.onMoodsFetched(fetched);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FirestoreError", "Failed to load user mood list", e);
                    listener.onMoodsFetched(new ArrayList<>());
                });
    }

    /**
     * For adding a new MoodEvent:
     *  1) Insert into local HashMap.
     *  2) If online & user is logged in => also store to Firestore.
     */
    public void addMoodEvent(MoodEvent newMood) {
        int id = newMood.getId();
        if (id <= 0) {
            // If user didn't supply a real ID, generate one
            id = nextLocalId++;
        }
        // Store in local
        moodEvents.put(id, newMood);

        final int finalId = id;

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w("MoodEventViewModel", "No user => cannot push to Firestore, only local");
            return;
        }
        if (isOnline()) {
            String userUid = currentUser.getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("createdAt", newMood.getCreatedAt());
            data.put("emotionalState", newMood.getEmotionalState());
            data.put("trigger", newMood.getTrigger());
            data.put("socialSituation", newMood.getSocialSituation());
            data.put("explanation", newMood.getExplanation());
            data.put("latitude", newMood.getLatitude());
            data.put("longitude", newMood.getLongitude());
            // If you want lastEditedAt, images, etc., add them

            db.collection("MoodEvents")
                    .document(String.valueOf(id))
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MoodEventViewModel", "Saved mood to Firestore with ID=" + finalId);

                        // Add the mood ID to the user's MoodEventIds list
                        db.collection("Users")
                                .whereEqualTo("email", currentUser.getEmail())
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        String username = snapshot.getDocuments().get(0).getString("username");

                                        db.collection("Users")
                                                .document(username)
                                                .update("MoodEventIds", com.google.firebase.firestore.FieldValue.arrayUnion(finalId))
                                                .addOnSuccessListener(unused -> Log.d("Firestore", "Mood ID added to user"))
                                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update user mood list", e));
                                    }
                                });
                        if (onAddListener != null) onAddListener.onAddSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("MoodEventViewModel", "Failed to save mood to Firestore", e);
                        if (onAddListener != null) onAddListener.onAddFailure(e.getMessage());
                    });
        } else {
            Log.d("MoodEventViewModel", "Offline => only local store. Will need sync later");
            if (onAddListener != null) onAddListener.onAddSuccess();
        }
    }

    // If you want an overload for the version used by MoodActivity
    public void addMoodEvent(String emoState,
                             String trigger,
                             String social,
                             String explanation,
                             Uri image,
                             double lat,
                             double lon,
                             OnAddListener listener) {
        // Build a new MoodEvent with ID=0 => triggers local ID
        MoodEvent newMood = new MoodEvent(
                0,
                Timestamp.now(),
                emoState,
                trigger,
                social,
                explanation,
                image,
                lat,
                lon
        );
        this.onAddListener = listener;
        addMoodEvent(newMood);
        this.onAddListener = null; // reset
    }

    // Use an internal field to pass success/failure back to the UI
    private OnAddListener onAddListener;

    public interface OnAddListener {
        void onAddSuccess();
        void onAddFailure(String errorMessage);
    }

    // For offline usage, or to retrieve local data
    public HashMap<Integer, MoodEvent> getMoodEvents() {
        return moodEvents;
    }

    public interface OnMoodsFetchedListener {
        void onMoodsFetched(List<MoodEvent> moodEvents);
    }

    public void deleteMoodEvent(int moodId, OnDeleteListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            moodEvents.remove(moodId);
            listener.onDeleteSuccess();
            return;
        }

        db.collection("MoodEvents").document(String.valueOf(moodId))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("Users")
                            .whereEqualTo("email", currentUser.getEmail())
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.isEmpty()) {
                                    String username = snapshot.getDocuments().get(0).getString("username");
                                    db.collection("Users").document(username)
                                            .update("MoodEventIds", com.google.firebase.firestore.FieldValue.arrayRemove(moodId))
                                            .addOnSuccessListener(unused -> {
                                                moodEvents.remove(moodId);
                                                listener.onDeleteSuccess();
                                            })
                                            .addOnFailureListener(e -> listener.onDeleteFailure(e.getMessage()));
                                }
                            });
                })
                .addOnFailureListener(e -> listener.onDeleteFailure(e.getMessage()));
    }

    public void updateMoodEvent(MoodEvent updatedEvent, OnUpdateListener listener) {
        int id = updatedEvent.getId();
        moodEvents.put(id, updatedEvent);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || !isOnline()) {
            listener.onUpdateSuccess();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("createdAt", updatedEvent.getCreatedAt());
        data.put("emotionalState", updatedEvent.getEmotionalState());
        data.put("trigger", updatedEvent.getTrigger());
        data.put("socialSituation", updatedEvent.getSocialSituation());
        data.put("explanation", updatedEvent.getExplanation());
        data.put("latitude", updatedEvent.getLatitude());
        data.put("longitude", updatedEvent.getLongitude());

        db.collection("MoodEvents").document(String.valueOf(id))
                .update(data)
                .addOnSuccessListener(aVoid -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
    }

    public interface OnDeleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }

    public interface OnUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }
}

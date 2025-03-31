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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoodEventViewModel extends ViewModel {

    private static MoodEventViewModel instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // Local HashMap for offline usage
    private final HashMap<String, MoodEvent> moodEvents = new HashMap<>();

    private Context context;

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

    public void fetchCurrentUserMoods(OnMoodsFetchedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
            return;
        }

        String email = currentUser.getEmail();
        if (!isOnline()) {
            listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
            return;
        }

        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        String username = userDoc.getString("username");

                        List<String> moodIds = (List<String>) userDoc.get("MoodEventIds");
                        if (moodIds == null || moodIds.isEmpty()) {
                            moodEvents.clear();
                            listener.onMoodsFetched(new ArrayList<>());
                            return;
                        }

                        List<MoodEvent> fetched = new ArrayList<>();
                        final int totalToFetch = moodIds.size();

                        for (String id : moodIds) {
                            db.collection("MoodEvents").document(id)
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            try {
                                                String docId = doc.getString("id");
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

                                        if (fetched.size() >= totalToFetch) {
                                            moodEvents.clear();
                                            for (MoodEvent m : fetched) {
                                                moodEvents.put(m.getMoodId(), m);
                                            }
                                            listener.onMoodsFetched(fetched);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onMoodsFetched(new ArrayList<>());
                });
    }

    public void addMoodEvent(String emoState, String trigger, String social,
                             String explanation, Uri image, double lat, double lon,
                             OnAddListener listener) {
        String uuid = UUID.randomUUID().toString();
        MoodEvent newMood = new MoodEvent(
                uuid,
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
        this.onAddListener = null;
    }

    public void addMoodEvent(MoodEvent newMood) {
        String id = newMood.getMoodId();
        moodEvents.put(id, newMood);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        if (isOnline()) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("createdAt", newMood.getCreatedAt());
            data.put("emotionalState", newMood.getEmotionalState());
            data.put("trigger", newMood.getTrigger());
            data.put("socialSituation", newMood.getSocialSituation());
            data.put("explanation", newMood.getExplanation());
            data.put("latitude", newMood.getLatitude());
            data.put("longitude", newMood.getLongitude());

            db.collection("MoodEvents")
                    .document(id)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("Users")
                                .whereEqualTo("email", currentUser.getEmail())
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        String username = snapshot.getDocuments().get(0).getString("username");
                                        db.collection("Users")
                                                .document(username)
                                                .update("MoodEventIds", com.google.firebase.firestore.FieldValue.arrayUnion(id));
                                    }
                                });
                        if (onAddListener != null) onAddListener.onAddSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (onAddListener != null) onAddListener.onAddFailure(e.getMessage());
                    });
        } else {
            if (onAddListener != null) onAddListener.onAddSuccess();
        }
    }

    private OnAddListener onAddListener;

    public interface OnAddListener {
        void onAddSuccess();
        void onAddFailure(String errorMessage);
    }

    public HashMap<String, MoodEvent> getMoodEvents() {
        return moodEvents;
    }

    public interface OnMoodsFetchedListener {
        void onMoodsFetched(List<MoodEvent> moodEvents);
    }

    public void deleteMoodEvent(String moodId, OnDeleteListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            moodEvents.remove(moodId);
            listener.onDeleteSuccess();
            return;
        }

        db.collection("MoodEvents").document(moodId)
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
        String id = updatedEvent.getMoodId();
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

        db.collection("MoodEvents").document(id)
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

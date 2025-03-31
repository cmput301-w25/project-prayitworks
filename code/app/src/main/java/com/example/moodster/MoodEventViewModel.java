package com.example.moodster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoodEventViewModel extends ViewModel {

    private static MoodEventViewModel instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final HashMap<String, MoodEvent> moodEvents = new HashMap<>();

    private Context context;
    private String currentUsername;

    private MoodEventViewModel() {}

    public static MoodEventViewModel getInstance() {
        if (instance == null) {
            instance = new MoodEventViewModel();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setUsername(String username) {
        this.currentUsername = username;
    }

    public String getUsername() {
        return currentUsername;
    }

    private boolean isOnline() {
        if (context == null) return true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    // ----------------------- FETCH -----------------------
    public void fetchCurrentUserMoods(OnMoodsFetchedListener listener) {
        if (!isOnline() || currentUsername == null) {
            listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
            return;
        }

        db.collection("Users")
                .document(currentUsername)
                .get()
                .addOnSuccessListener(userDoc -> {
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
                                            MoodEvent moodEvent = new MoodEvent(
                                                    doc.getString("id"),
                                                    doc.getTimestamp("createdAt"),
                                                    doc.getString("emotionalState"),
                                                    doc.getString("trigger"),
                                                    doc.getString("socialSituation"),
                                                    doc.getString("explanation"),
                                                    null,
                                                    doc.contains("latitude") ? doc.getDouble("latitude") : 0.0,
                                                    doc.contains("longitude") ? doc.getDouble("longitude") : 0.0,
                                                    doc.contains("isPublic") ? doc.getBoolean("isPublic") : true
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
                })
                .addOnFailureListener(e -> listener.onMoodsFetched(new ArrayList<>()));
    }

    // ----------------------- ADD -----------------------
    public void addMoodEvent(String emoState, String trigger, String social,
                             String explanation, Uri image, double lat, double lon, boolean isPublic,
                             OnAddListener listener) {

        String id = UUID.randomUUID().toString();
        MoodEvent newMood = new MoodEvent(id, Timestamp.now(), emoState, trigger, social, explanation, image, lat, lon, isPublic);
        this.onAddListener = listener;
        addMoodEvent(newMood);
        this.onAddListener = null;
    }

    public void addMoodEvent(MoodEvent newMood) {
        String id = newMood.getMoodId();
        moodEvents.put(id, newMood);

        if (!isOnline() || currentUsername == null) {
            if (onAddListener != null) onAddListener.onAddSuccess();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("createdAt", newMood.getCreatedAt());
        data.put("emotionalState", newMood.getEmotionalState());
        data.put("trigger", newMood.getTrigger());
        data.put("socialSituation", newMood.getSocialSituation());
        data.put("explanation", newMood.getExplanation());
        data.put("latitude", newMood.getLatitude());
        data.put("longitude", newMood.getLongitude());
        data.put("isPublic", newMood.isPublic());

        db.collection("MoodEvents")
                .document(id)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    db.collection("Users")
                            .document(currentUsername)
                            .update("MoodEventIds", com.google.firebase.firestore.FieldValue.arrayUnion(id));
                    if (onAddListener != null) onAddListener.onAddSuccess();
                })
                .addOnFailureListener(e -> {
                    if (onAddListener != null) onAddListener.onAddFailure(e.getMessage());
                });
    }

    // ----------------------- DELETE -----------------------
    public void deleteMoodEvent(String moodId, OnDeleteListener listener) {
        moodEvents.remove(moodId);

        if (!isOnline() || currentUsername == null) {
            listener.onDeleteSuccess();
            return;
        }

        db.collection("MoodEvents").document(moodId).delete()
                .addOnSuccessListener(aVoid -> db.collection("Users")
                        .document(currentUsername)
                        .update("MoodEventIds", com.google.firebase.firestore.FieldValue.arrayRemove(moodId))
                        .addOnSuccessListener(unused -> listener.onDeleteSuccess())
                        .addOnFailureListener(e -> listener.onDeleteFailure(e.getMessage())))
                .addOnFailureListener(e -> listener.onDeleteFailure(e.getMessage()));
    }

    // ----------------------- UPDATE -----------------------
    public void updateMoodEvent(MoodEvent updatedEvent, OnUpdateListener listener) {
        String id = updatedEvent.getMoodId();
        moodEvents.put(id, updatedEvent);

        if (!isOnline()) {
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
        data.put("isPublic", updatedEvent.isPublic());

        db.collection("MoodEvents").document(id).update(data)
                .addOnSuccessListener(aVoid -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
    }

    // ----------------------- Interfaces -----------------------
    private OnAddListener onAddListener;

    public interface OnMoodsFetchedListener {
        void onMoodsFetched(List<MoodEvent> moodEvents);
    }

    public interface OnAddListener {
        void onAddSuccess();
        void onAddFailure(String errorMessage);
    }

    public interface OnDeleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }

    public interface OnUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public HashMap<String, MoodEvent> getMoodEvents() {
        return moodEvents;
    }
}

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
        String userUid = currentUser.getUid();

        if (!isOnline()) {
            Log.d("MoodEventViewModel", "Offline => returning local moods");
            listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
            return;
        }

        // Online => fetch from Firestore
        db.collection("MoodEvents")
                .whereEqualTo("authorUsername", userUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<MoodEvent> result = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
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
                        result.add(moodEvent);
                    }
                    // Update local map
                    moodEvents.clear();
                    for (MoodEvent m : result) {
                        moodEvents.put(m.getId(), m);
                    }
                    Log.d("MoodEventViewModel", "Fetched " + result.size() + " moods from Firestore");

                    listener.onMoodsFetched(result);
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodEventViewModel", "Failed Firestore fetch => fallback to local", e);
                    listener.onMoodsFetched(new ArrayList<>(moodEvents.values()));
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
            data.put("authorUsername", userUid);
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
}

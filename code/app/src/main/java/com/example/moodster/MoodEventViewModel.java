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
    private final HashMap<Integer, MoodEvent> moodEvents = new HashMap<>();
    private int nextId = 1; // Unique ID counter
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<MoodEvent> fetchedMoods = new ArrayList<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private Context context;

    // Set context for connectivity check
    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    // Check if device is online
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // Fetch user details by username
    public void fetchUserDetails(String username, OnUserFetchedListener listener) {
        db.collection("users").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        listener.onUserFetched(userData);
                    } else {
                        listener.onUserFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodEventViewModel", "Error fetching user details", e);
                    listener.onUserFetched(null);
                });
    }

    // Add a new mood event
    public void addMoodEvent(String emotionalState, String trigger, String socialSituation, String explanation, Uri image, OnAddListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w("MoodEventViewModel", "User not authenticated");
            listener.onAddFailure("User not authenticated");
            return;
        }
        String authorUsername = currentUser.getUid(); // Use UID or username based on auth setup
        Timestamp createdAt = Timestamp.now();
        MoodEvent moodEvent = new MoodEvent(nextId, createdAt, emotionalState, trigger, socialSituation, explanation, image);
        Map<String, Object> data = new HashMap<>();
        data.put("authorUsername", authorUsername);
        data.put("id", nextId);
        data.put("createdAt", createdAt);
        data.put("lastEditedAt", createdAt);
        data.put("emotionalState", emotionalState);
        data.put("trigger", trigger);
        data.put("socialSituation", socialSituation);
        data.put("explanation", explanation);
        //data.put("Image", image); We might have to save this


        if (isOnline()) {
            // Online: Direct database update
            db.collection("moods")
                    .document(String.valueOf(nextId))
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        nextId++;
                        listener.onAddSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error adding mood online", e);
                        moodEvents.put(nextId, moodEvent); // Cache for retry
                        listener.onAddFailure(e.getMessage());
                        queueForSync(data, "add", nextId); // Queue for sync
                    });
        } else {
            // Offline: Store in HashMap and queue for sync
            moodEvents.put(nextId, moodEvent);
            nextId++; // Increment for next offline ID
            listener.onAddSuccess(); // Success locally, sync later
            queueForSync(data, "add", nextId - 1); // Queue for sync when online
        }
    }

    // Update an existing mood event
    public void updateMoodEvent(int moodId, String emotionalState, String trigger, String socialSituation, String explanation, OnUpdateListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onUpdateFailure("User not authenticated");
            return;
        }
        String currentUsername = currentUser.getUid(); // Use UID or username based on auth

        db.collection("moods")
                .document(String.valueOf(moodId))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String authorUsername = documentSnapshot.getString("authorUsername");
                        if (currentUsername.equals(authorUsername)) {
                            Timestamp lastEditedAt = Timestamp.now();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("emotionalState", emotionalState);
                            updates.put("trigger", trigger);
                            updates.put("socialSituation", socialSituation);
                            updates.put("explanation", explanation);
                            updates.put("lastEditedAt", lastEditedAt);

                            if (isOnline()) {
                                // Online: Direct database update
                                db.collection("moods")
                                        .document(String.valueOf(moodId))
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            listener.onUpdateSuccess();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("MoodEventViewModel", "Error updating mood online", e);
                                            MoodEvent moodEvent = moodEvents.get(moodId);
                                            if (moodEvent != null) {
                                                moodEvent.setEmotionalState(emotionalState);
                                                moodEvent.setTrigger(trigger);
                                                moodEvent.setSocialSituation(socialSituation);
                                                moodEvent.setExplanation(explanation);
                                            }
                                            listener.onUpdateFailure(e.getMessage());
                                            queueForSync(updates, "update", moodId); // Queue for sync
                                        });
                            } else {
                                // Offline: Update HashMap and queue for sync
                                MoodEvent moodEvent = moodEvents.get(moodId);
                                if (moodEvent != null) {
                                    moodEvent.setEmotionalState(emotionalState);
                                    moodEvent.setTrigger(trigger);
                                    moodEvent.setSocialSituation(socialSituation);
                                    moodEvent.setExplanation(explanation);
                                }
                                listener.onUpdateSuccess(); // Success locally, sync later
                                queueForSync(updates, "update", moodId); // Queue for sync when online
                            }
                        } else {
                            listener.onUpdateFailure("Only the author can edit this mood");
                        }
                    } else {
                        listener.onUpdateFailure("Mood not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodEventViewModel", "Error checking mood", e);
                    listener.onUpdateFailure(e.getMessage());
                });
    }

    // Fetch and filter moods for followed users (Online only)
    public void fetchAndFilterMoods(String currentUsername, String keyword, OnMoodsFilteredListener listener) {
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            listener.onMoodsFiltered(new ArrayList<>());
            return;
        }

        if (!isOnline()) {
            listener.onMoodsFiltered(new ArrayList<>()); // Return empty if offline
            return;
        }

        db.collection("users").document(currentUsername)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following == null || following.isEmpty()) {
                        listener.onMoodsFiltered(new ArrayList<>());
                        return;
                    }

                    db.collection("moods")
                            .whereIn("authorUsername", following)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                fetchedMoods.clear();
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                    fetchedMoods.add(moodEvent);
                                }
                                List<MoodEvent> filteredMoods = filterByExplanation(keyword);
                                listener.onMoodsFiltered(filteredMoods);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("MoodEventViewModel", "Error fetching moods online", e);
                                listener.onMoodsFiltered(new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodEventViewModel", "Error fetching following list", e);
                    listener.onMoodsFiltered(new ArrayList<>());
                });
    }

    private List<MoodEvent> filterByExplanation(String keyword) {
        List<MoodEvent> filteredMoods = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredMoods.addAll(fetchedMoods);
        } else {
            String lowerCaseKeyword = keyword.toLowerCase();
            for (MoodEvent mood : fetchedMoods) {
                if (mood.getExplanation() != null && mood.getExplanation().toLowerCase().contains(lowerCaseKeyword)) {
                    filteredMoods.add(mood);
                }
            }
        }
        return filteredMoods.isEmpty() ? new ArrayList<>() : filteredMoods;
    }

    // Queue changes for sync when online
    private void queueForSync(Map<String, Object> data, String operation, int moodId) {
        // Placeholder: Log the queued operation
        Log.d("OfflineQueue", "Queued " + operation + " for moodId " + moodId + ": " + data);
        // In a real app, use a local database (e.g., Room) to persist queued changes
        // and sync them in a background task when online
    }

    public void addMoodEvent(MoodEvent newMood) {

    }

    public interface OnUserFetchedListener {
        void onUserFetched(Map<String, Object> userData);
    }

    public interface OnMoodsFilteredListener {
        void onMoodsFiltered(List<MoodEvent> filteredMoods);
    }

    public interface OnUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    public interface OnAddListener {
        void onAddSuccess();
        void onAddFailure(String errorMessage);
    }

    // Getter for offline cache (optional, for offline use if needed)
    public HashMap<Integer, MoodEvent> getMoodEvents() {
        return moodEvents;
    }
}
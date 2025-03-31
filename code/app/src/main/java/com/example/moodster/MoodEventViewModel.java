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

/**
 * The MoodEventViewModel class serves as a ViewModel that manages MoodEvent data for the current user
 * and interacts with Firebase. It provides methods to fetch, add, delete, and update mood events,
 * and maintains an internal cache of mood events in a HashMap. This ViewModel also handles online
 * connectivity checks and user context management.
 */
public class MoodEventViewModel extends ViewModel {

    private static MoodEventViewModel instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final HashMap<String, MoodEvent> moodEvents = new HashMap<>();

    private Context context;
    private String currentUsername;

    private MoodEventViewModel() {}

    /**
     * Returns the singleton instance of MoodEventViewModel.
     *
     * @return the MoodEventViewModel instance
     */
    public static MoodEventViewModel getInstance() {
        if (instance == null) {
            instance = new MoodEventViewModel();
        }
        return instance;
    }

    /**
     * Sets the context for the ViewModel.
     *
     * @param context
     *      the Context to be used, stored as an application context
     */
    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Sets the current users username.
     *
     * @param username
     *      the username of the current user
     */
    public void setUsername(String username) {
        this.currentUsername = username;
    }

    /**
     * Returns the current users username.
     *
     * @return
     *      the current username as a String
     */
    public String getUsername() {
        return currentUsername;
    }

    /**
     * Checks if the device is online.
     *
     * @return true if the device has an active network connection, false otherwise
     */
    private boolean isOnline() {
        if (context == null) return true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    // ----------------------- FETCH -----------------------
    /**
     * Fetches the current user's mood events from Firebase.
     *
     * <p>If the device is offline or the username is not set, the listener is invoked with the locally cached mood events.
     * Otherwise, the method retrieves the list of mood event IDs from the user's document in Firestore,
     * fetches each MoodEvent, updates the local cache, and invokes the listener with the fetched list.</p>
     *
     * @param listener
     *      a callback to receive the list of MoodEvent objects
     */
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
    /**
     * Adds a new mood event with the provided details.
     *
     * <p>This method creates a new MoodEvent with a generated unique ID and the current timestamp,
     * then calls the overloaded addMoodEvent method to save it.</p>
     *
     * @param emoState
     *      the emotional state of the mood event
     * @param trigger
     *      the trigger associated with the mood event
     * @param social
     *      the social situation during the mood event
     * @param explanation
     *      the explanation for the mood event
     * @param image
     *      the URI of an image associated with the mood event
     * @param lat
     *      the latitude where the event occurred
     * @param lon
     *      the longitude where the event occurred
     * @param isPublic
     *      whether the mood event is public
     * @param listener
     *      a callback to notify success or failure of the operation
     */
    public void addMoodEvent(String emoState, String trigger, String social,
                             String explanation, Uri image, double lat, double lon, boolean isPublic,
                             OnAddListener listener) {

        String id = UUID.randomUUID().toString();
        MoodEvent newMood = new MoodEvent(id, Timestamp.now(), emoState, trigger, social, explanation, image, lat, lon, isPublic);
        this.onAddListener = listener;
        addMoodEvent(newMood);
        this.onAddListener = null;
    }

    /**
     * Adds the provided MoodEvent to the local cache and Firebase.
     *
     * <p>If the device is offline or the username is not set, the listener is notified immediately.
     * Otherwise, the mood event is stored in Firebase and the mood event ID is appended to the user's document.</p>
     *
     * @param newMood
     *      the MoodEvent to be added
     */
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
    /**
     * Deletes a mood event with the specified ID.
     *
     * <p>This method removes the mood event from the local cache and, if online, deletes it from Firebase.
     * It also removes the mood event ID from the user's document in Firebase.</p>
     *
     * @param moodId
     *      the ID of the mood event to delete
     * @param listener
     *      a callback to notify success or failure of the deletion
     */
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
    /**
     * Updates an existing mood event in Firebase.
     *
     * <p>This method updates the specified MoodEvent in the local cache and Firestore.
     * If offline, the listener is immediately notified of success.</p>
     *
     * @param updatedEvent
     *      the MoodEvent with updated information
     * @param listener
     *      a callback to notify success or failure of the update
     */
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

    /**
     * Listener interface for fetching mood events.
     */
    public interface OnMoodsFetchedListener {
        void onMoodsFetched(List<MoodEvent> moodEvents);
    }

    /**
     * Listener interface for adding a mood event.
     */
    public interface OnAddListener {
        void onAddSuccess();
        void onAddFailure(String errorMessage);
    }

    /**
     * Listener interface for deleting a mood event.
     */
    public interface OnDeleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(String errorMessage);
    }

    /**
     * Listener interface for updating a mood event.
     */
    public interface OnUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(String errorMessage);
    }

    /**
     * Returns the internal map of mood events.
     *
     * @return
     *      a HashMap mapping mood event IDs to moodEvents
     */
    public HashMap<String, MoodEvent> getMoodEvents() {
        return moodEvents;
    }
}

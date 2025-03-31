package com.example.moodster;

import android.net.Uri;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * The MoodEvent class represents an individual mood event in the Moodster app.
 * A MoodEvent stores details about an emotional event including its unique identifier, emotional state,
 * timestamps for creation and last edit, a trigger, social situation, an explanation, an optional image, location
 * coordinates, and its public visibility status.
 */
public class MoodEvent implements Serializable {
    private final String moodId;
    private String emotionalState; // Required
    private  Timestamp createdAt; // Stores date & time
    private  Timestamp lastEditAt;
    private String trigger; // Optional
    private String socialSituation; // Optional
    private String explanation; // Express reason why

    // For image
    private Uri image;

    // For Location
    private double latitude;
    private double longitude;
    private boolean isPublic;


    // Predefined list of valid emotional states
    public static final List<String> VALID_EMOTIONAL_STATES = Arrays.asList(
            "Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"
    );

    /**
     * Constructor for MoodEvent
     *
     * @param moodId
     *      the unique identifier for the mood event
     * @param createdAt
     *      the timestamp when the mood event was created
     * @param emotionalState
     *      the emotional state of the event
     * @param trigger
     *      an optional trigger for the mood event
     * @param socialSituation
     *      an optional description of the social situation during the event
     * @param explanation
     *      an optional explanation for the mood event
     * @param image
     *      the URI of an image associated with the event
     * @param lat
     *      he latitude where the event occurred
     * @param lon
     *      the longitude where the event occurred
     * @param isPublic
     *      true if the mood event is public; false otherwise
     *
     * @throws IllegalArgumentException
     *      if the provided emotional state is not valid
     */
    public MoodEvent(String moodId, Timestamp createdAt, String emotionalState, String trigger, String socialSituation, String explanation, Uri image, double lat, double lon, boolean isPublic) {
        if (!VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
        this.moodId = moodId;
        this.emotionalState = emotionalState;
        this.createdAt = createdAt; //added created At
        //if lastEditAt is not mentioned then it simply takes in the value of creatd At by default
        this.lastEditAt = lastEditAt != null ? lastEditAt : createdAt;
        // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx //
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.explanation = explanation;
        this.image = image;
        this.longitude = lon;
        this.latitude = lat;
        this.isPublic = isPublic;
    }

    /**
     * Returns the emoji representation corresponding to the mood event's emotional state.
     *
     * @return
     *      a String containing an emoji that coressponds the emotional state
     */
    public String getEmoji() {
        switch (emotionalState) {
            case "Anger":
                return "😡";
            case "Confusion":
                return "😵‍💫";
            case "Disgust":
                return "🤢";
            case "Fear":
                return "😨";
            case "Happiness":
                return "😁";
            case "Sadness":
                return "😓";
            case "Shame":
                return "😶‍🌫️"; // Flushed Face (used here to represent shame)
            case "Surprise":
                return "😮"; // Astonished Face
            default:
                return "😶"; // Neutral Face for undefined moods
        }
    }

    /**
     * Returns the unique identifier for this mood event.
     *
     * @return
     *      the mood event ID
     */
    public String getMoodId() { return moodId; }

    /**
     * Returns the emotional state of this mood event.
     *
     * @return
     *      the emotional state as a String
     */
    public String getEmotionalState() { return emotionalState; }

    /**
     * Returns the timestamp when this mood event was created.
     *
     * @return
     *      the createdAt Timestamp
     */
    public Timestamp getCreatedAt() { return createdAt; }

    /**
     * Returns the timestamp when this mood event was last edited.
     *
     * @return
     *      the lastEditAt Timestamp
     */
    public Timestamp getLastEditAt() {return lastEditAt;
    }

    /**
     * Returns the trigger associated with this mood event.
     *
     * @return
     *      the trigger as a String
     */
    public String getTrigger() { return trigger; }

    /**
     * Returns the social situation during this mood event.
     *
     * @return
     *      the social situation as a String
     */
    public String getSocialSituation() { return socialSituation; }

    /**
     * Returns the explanation for this mood event.
     *
     * @return
     *      the explanation as a String
     */
    public String getExplanation() { return explanation; }

    /**
     * Returns the URI of the image associated with this mood event.
     *
     * @return
     *      the image URI
     */
    public Uri getImage(){return image; }

    /**
     * Returns the latitude of the location where the mood event occurred.
     *
     * @return
     *      the latitude as a double
     */
    public double getLatitude() { return latitude; }

    /**
     * Returns the longitude of the location where the mood event occurred.
     *
     * @return
     *      the longitude as a double
     */
    public double getLongitude() { return longitude; }

    /**
     * Returns the public visibility status of this mood event.
     *
     * @return
     *      true if the event is public, false otherwise
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Updates the emotional state of this mood event.
     *
     * <p>If the provided emotional state is valid, it sets the new emotional state and updates the last edited timestamp.</p>
     *
     * @param emotionalState
     *      the new emotional state
     *
     * @throws IllegalArgumentException
     *      if the provided emotional state is invalid
     */
    public void setEmotionalState(String emotionalState) {
        if (VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            this.emotionalState = emotionalState;
            this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
        } else {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
    }

    /**
     * Updates the trigger of this mood event.
     *
     * <p>This method sets the new trigger and updates the last edited timestamp.</p>
     *
     * @param trigger
     *      the new trigger as a String
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
        this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
    }

    /**
     * Updates the social situation of this mood event.
     *
     * <p>This method sets the new social situation and updates the last edited timestamp.</p>
     *
     * @param socialSituation
     *      the new social situation as a String
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
        this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
    }

    /**
     * Updates the explanation for this mood event.
     *
     * <p>This method sets the new explanation and updates the last edited timestamp.</p>
     *
     * @param explanation
     *      the new explanation as a String
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
        this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
    }

    /**
     * Sets the image URI for this mood event.
     *
     * @param Image
     *      the new image URI
     */
    public void setImage(Uri Image){
        this.image = Image;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.lastEditAt = Timestamp.now();
    }

    /**
     * Returns a string representation of the mood event.
     *
     * @return
     *      a String containing details about the mood event
     */
    @Override
    public String toString() {
        return "MoodEvent{" +
                "id=" + moodId +
                ", emotionalState='" + emotionalState + '\'' +
                ", createdAt=" + createdAt.toDate() +
                ", lastEditAt=" + lastEditAt.toDate()+
                ", trigger='" + trigger + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", explanation='" + explanation + '\'' +
                ", lon= " + longitude + '\'' +
                ", lat= " + latitude + '\'' +
                ", isPublic=" + isPublic +
                "}";

    }
}

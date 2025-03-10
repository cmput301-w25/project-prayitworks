package com.example.moodster;

import android.net.Uri;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MoodEvent implements Serializable {
    private final int id; // Unique ID
    private String emotionalState; // Required
    private  Timestamp createdAt; // Stores date & time
    private  Timestamp lastEditAt;
    private String trigger; // Optional
    private String socialSituation; // Optional
    private String explanation; // Express reason why

    // For image
    private Uri image;

    // For Location
    private double latitude = 0.0;
    private double longitude = 0.0;


    // Predefined list of valid emotional states
    public static final List<String> VALID_EMOTIONAL_STATES = Arrays.asList(
            "Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"
    );

    // Constructor
    public MoodEvent(int id, Timestamp createdAt, String emotionalState, String trigger, String socialSituation, String explanation, Uri image, double latitude, double longitude) {
        if (!VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
        this.id = id;
        this.emotionalState = emotionalState;
        this.createdAt = createdAt; //added created At
        //if lastEditAt is not mentioned then it simply takes in the value of creatd At by default
        this.lastEditAt = lastEditAt != null ? lastEditAt : createdAt;
        // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx //
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.explanation = explanation;
        this.image = image;
        this.longitude = longitude;
        this.latitude = latitude;
    }

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

    public int getId() { return id; }
    public String getEmotionalState() { return emotionalState; }

    public Timestamp getCreatedAt() { return createdAt; }

    public Timestamp getLastEditAt() {return lastEditAt;
    }

    public String getTrigger() { return trigger; }
    public String getSocialSituation() { return socialSituation; }
    public String getExplanation() { return explanation; }

    public Uri getImage(){return image; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public void setExplanation() { this.explanation = explanation; }

    // Setters for editable fields
    public void setEmotionalState(String emotionalState) {
        if (VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            this.emotionalState = emotionalState;
            this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
        } else {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
    }
    public void setTrigger(String trigger) {
        this.trigger = trigger;
        this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
    }
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
        this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
        this.lastEditAt = Timestamp.now(); // Update lastEditedAt on change
    }

    public void setImage(Uri Image){
        this.image = Image;
    }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id=" + id +
                ", emotionalState='" + emotionalState + '\'' +
                ", createdAt=" + createdAt.toDate() +
                ", lastEditAt=" + lastEditAt.toDate()+
                ", trigger='" + trigger + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';

    }
}

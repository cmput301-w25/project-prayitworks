package com.example.moodster;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MoodEvent implements Serializable {
    private final int id; // Unique ID
    private final String emotionalState; // Required
    private final Timestamp createdAt; // Stores date & time
    private final Timestamp lastEditAt;
    private final String trigger; // Optional
    private final String socialSituation; // Optional
    private String explanation; // Express reason why

    // Predefined list of valid emotional states
    public static final List<String> VALID_EMOTIONAL_STATES = Arrays.asList(
            "Anger 😡", "Confusion 😕", "Disgust 🤢", "Fear 😨", "Happiness 😁", "Sadness 😓", "Shame 😶‍🌫️", "Surprise 😮"
    );
    // Constructor
    public MoodEvent(int id, String emotionalState, long timestamp, String trigger, String socialSituation, String explanation) {
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
    }

    public int getId() { return id; }
    public String getEmotionalState() { return emotionalState; }

    public Timestamp getCreatedAt() { return createdAt; }

    public Timestamp getLastEditAt() {return lastEditAt;
    }

    public String getTrigger() { return trigger; }
    public String getSocialSituation() { return socialSituation; }
    public String getExplanation() { return explanation; }
    public void setExplanation() { this.explanation = explanation; }

    // Setters for editable fields
    public void setEmotionalState(String emotionalState) {
        if (VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            this.emotionalState = emotionalState;
            this.lastEditedAt = Timestamp.now(); // Update lastEditedAt on change
        } else {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
    }
    public void setTrigger(String trigger) {
        this.trigger = trigger;
        this.lastEditedAt = Timestamp.now(); // Update lastEditedAt on change
    }
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
        this.lastEditedAt = Timestamp.now(); // Update lastEditedAt on change
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
        this.lastEditedAt = Timestamp.now(); // Update lastEditedAt on change
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

package com.example.moodster;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MoodEvent implements Serializable {
    private final int id; // Unique ID
    private final String emotionalState; // Required
    private final long timestamp; // Stores date & time
    private final String trigger; // Optional
    private final String socialSituation; // Optional
    private String explanation; // Express reason why

    // Predefined list of valid emotional states
    public static final List<String> VALID_EMOTIONAL_STATES = Arrays.asList(
            "Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"
    );
    // Constructor
    public MoodEvent(int id, String emotionalState, long timestamp, String trigger, String socialSituation, String explanation) {
        if (!VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
        this.id = id;
        this.emotionalState = emotionalState;
        this.timestamp = timestamp;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public String getEmotionalState() { return emotionalState; }
    public long getTimestamp() { return timestamp; }
    public String getTrigger() { return trigger; }
    public String getSocialSituation() { return socialSituation; }
    public String getExplanation() { return explanation; }
    public void setExplanation() { this.explanation = explanation; }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id=" + id +
                ", emotionalState='" + emotionalState + '\'' +
                ", timestamp=" + timestamp +
                ", trigger='" + trigger + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}

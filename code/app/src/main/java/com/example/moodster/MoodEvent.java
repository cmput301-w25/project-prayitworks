package com.example.moodster;
import java.util.Arrays;
import java.util.List;

public class MoodEvent {
    private final int id; // Unique ID
    private final String emotionalState; // Required
    private final long timestamp; // Stores date & time
    private final String trigger; // Optional
    private final String socialSituation; // Optional

    // Predefined list of valid emotional states
    public static final List<String> VALID_EMOTIONAL_STATES = Arrays.asList(
            "Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"
    );

    public MoodEvent(int id, String emotionalState, long timestamp, String trigger, String socialSituation) {
        if (!VALID_EMOTIONAL_STATES.contains(emotionalState)) {
            throw new IllegalArgumentException("Invalid emotional state: " + emotionalState);
        }
        this.id = id;
        this.emotionalState = emotionalState;
        this.timestamp = timestamp;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
    }

    public int getId() { return id; }
    public String getEmotionalState() { return emotionalState; }
    public long getTimestamp() { return timestamp; }
    public String getTrigger() { return trigger; }
    public String getSocialSituation() { return socialSituation; }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id=" + id +
                ", emotionalState='" + emotionalState + '\'' +
                ", timestamp=" + timestamp +
                ", trigger='" + trigger + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                '}';
    }
}

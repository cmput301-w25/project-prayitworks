package com.example.moodster;
import androidx.lifecycle.ViewModel;
import java.util.HashMap;

public class MoodEventViewModel extends ViewModel {
    private final HashMap<Integer, MoodEvent> moodEvents = new HashMap<>();
    private int nextId = 1; // Unique ID counter


    public void addMoodEvent(String emotionalState, String trigger, String socialSituation, String explanation) {
        long timestamp = System.currentTimeMillis(); // Take the current Time
        MoodEvent moodEvent = new MoodEvent(nextId, emotionalState, timestamp, trigger, socialSituation, explanation);
        moodEvents.put(nextId, moodEvent);
        nextId++; // Increment ID for next event
    }
    public void addMoodEvent(MoodEvent moodEvent) {
        moodEvents.put(moodEvent.getId(), moodEvent);
    }


    public HashMap<Integer, MoodEvent> getMoodEvents() {
        return moodEvents;
    }

}

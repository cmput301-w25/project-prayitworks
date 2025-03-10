package com.example.moodster;

import java.util.ArrayList;
import java.util.List;

public class MoodEventFilter {
    public static List<MoodEvent> filterByExplanation(List<MoodEvent> masterList, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(masterList);
        }
        String lowerKey = keyword.toLowerCase();
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent event : masterList) {
            String explanation = event.getExplanation();
            if (explanation != null && explanation.toLowerCase().contains(lowerKey)) {
                filtered.add(event);
            }
        }
        return filtered;
    }
}

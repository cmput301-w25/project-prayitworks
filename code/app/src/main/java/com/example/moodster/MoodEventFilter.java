package com.example.moodster;

import java.util.ArrayList;
import java.util.List;
public class MoodEventFilter {
    public static List<MoodEvent> filterByExplanation(List<MoodEvent> masterList, String keyword) {
        List<MoodEvent> filtered = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            filtered.addAll(masterList);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (MoodEvent event : masterList) {
                if (event.getExplanation() != null && event.getExplanation().toLowerCase().contains(lowerKeyword)) {
                    filtered.add(event);
                }
            }
        }
        return filtered;
    }
}

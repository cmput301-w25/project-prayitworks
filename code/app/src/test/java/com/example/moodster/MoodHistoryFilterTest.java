package com.example.moodster;

import com.google.firebase.Timestamp;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MoodHistoryFilterTest {

    private List<MoodEvent> allEvents;
    private long now;
    private long sevenDaysAgo;

    @Before
    public void setup() {
        now = System.currentTimeMillis();
        sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000;

        allEvents = new ArrayList<>();

        // Add moods with varied valid emotional states
        allEvents.add(new MoodEvent(1, Timestamp.now(), "Happiness", "triggerA", "With a crowd", "Feeling great", null, 0, 0));
        allEvents.add(new MoodEvent(2, Timestamp.now(), "Happiness", "triggerB", "Alone", "So happy", null, 0, 0));
        allEvents.add(new MoodEvent(3, Timestamp.now(), "Sadness", "triggerC", "With a crowd", "Very sad", null, 0, 0));
        allEvents.add(new MoodEvent(4, Timestamp.now(), "Fear", "triggerD", "With someone", "Anxious", null, 0, 0));
        allEvents.add(new MoodEvent(5, Timestamp.now(), "Anger", "triggerE", "Alone", "Furious", null, 0, 0));

        // Older than 7 days
        long oldMillis = sevenDaysAgo - (2L * 24 * 60 * 60 * 1000);
        allEvents.add(new MoodEvent(6, new Timestamp(oldMillis / 1000, 0), "Disgust", "triggerF", "With someone", "Grossed out", null, 0, 0));

        // Edge: Exactly on the 7-day boundary
        allEvents.add(new MoodEvent(7, new Timestamp(sevenDaysAgo / 1000, 0), "Shame", "triggerG", "With someone", "Embarrassed", null, 0, 0));

        // Null and empty edge cases
        allEvents.add(new MoodEvent(8, Timestamp.now(), "Surprise", null, null, null, null, 0, 0));
        allEvents.add(new MoodEvent(9, Timestamp.now(), "Surprise", "", "", "", null, 0, 0));
    }

    @Test
    public void testMostRecentWeekFilter() {
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            long time = e.getCreatedAt().toDate().getTime();
            if (time >= sevenDaysAgo) {
                filtered.add(e);
            }
        }
        assertTrue(filtered.size() > 0);
        for (MoodEvent e : filtered) {
            assertTrue(e.getCreatedAt().toDate().getTime() >= sevenDaysAgo);
        }
    }

    @Test
    public void testFilterByEmotionalStateMultiple() {
        String keyword = "happiness";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            if (e.getEmotionalState().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(e);
            }
        }

        for (MoodEvent e : filtered) {
            assertTrue(e.getEmotionalState().toLowerCase().contains(keyword.toLowerCase()));
        }
    }

    @Test
    public void testFilterBySocialSituationExact() {
        String keyword = "With a crowd";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            String ss = e.getSocialSituation();
            if (ss != null && ss.equals(keyword)) {
                filtered.add(e);
            }
        }

        for (MoodEvent e : filtered) {
            assertEquals(keyword, e.getSocialSituation());
        }
    }

    @Test
    public void testFilterByReasonPartialMatch() {
        String keyword = "sad";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            String reason = e.getExplanation();
            if (reason != null && reason.toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(e);
            }
        }

        for (MoodEvent e : filtered) {
            assertTrue(e.getExplanation().toLowerCase().contains(keyword.toLowerCase()));
        }
    }

    @Test
    public void testFilterByTriggerNoMatch() {
        String keyword = "nonexistent";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            String trigger = e.getTrigger();
            if (trigger != null && trigger.toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(e);
            }
        }

        assertEquals(0, filtered.size());
    }

    @Test
    public void testTriggerWhitespaceHandling() {
        String keyword = "triggera";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            String trigger = e.getTrigger();
            if (trigger != null && trigger.toLowerCase().trim().contains(keyword.toLowerCase())) {
                filtered.add(e);
            }
        }

        for (MoodEvent e : filtered) {
            assertNotNull(e.getTrigger());
            assertTrue(e.getTrigger().toLowerCase().contains("triggera"));
        }
    }

    @Test
    public void testNullFieldsSafeCheck() {
        try {
            for (MoodEvent e : allEvents) {
                if (e.getExplanation() != null) {
                    e.getExplanation().toLowerCase();
                }
                if (e.getTrigger() != null) {
                    e.getTrigger().toLowerCase();
                }
                if (e.getSocialSituation() != null) {
                    e.getSocialSituation().toLowerCase();
                }
            }
        } catch (Exception e) {
            fail("Null-safe access failed: " + e.getMessage());
        }
    }

    @Test
    public void testSearchWithEmojiInExplanation() {
        MoodEvent emojiMood = new MoodEvent(10, Timestamp.now(), "Happiness", "triggerX", "Alone", "I feel 😊 today", null, 0, 0);
        allEvents.add(emojiMood);

        String keyword = "😊";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            String reason = e.getExplanation();
            if (reason != null && reason.contains(keyword)) {
                filtered.add(e);
            }
        }

        assertTrue(filtered.size() >= 1);
    }

    @Test
    public void testFilterEmptyExplanationField() {
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            if (e.getExplanation() == null || e.getExplanation().isEmpty()) {
                filtered.add(e);
            }
        }

        for (MoodEvent e : filtered) {
            assertTrue(e.getExplanation() == null || e.getExplanation().isEmpty());
        }
    }

    @Test
    public void testFilterWithUppercaseTrigger() {
        String keyword = "TRIGGERA";
        List<MoodEvent> filtered = new ArrayList<>();
        for (MoodEvent e : allEvents) {
            String trigger = e.getTrigger();
            if (trigger != null && trigger.toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(e);
            }
        }

        assertTrue(filtered.size() > 0);
    }
}

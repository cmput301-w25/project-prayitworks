package com.example.moodster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentRepository {

    private static CommentRepository instance;

    private final Map<String, List<CommentItem>> commentStorage = new HashMap<>();

    private CommentRepository() {}

    public static CommentRepository getInstance() {
        if (instance == null) {
            instance = new CommentRepository();
        }
        return instance;
    }

    public List<CommentItem> getCommentsForMood(String moodEventId) {
        return commentStorage.getOrDefault(moodEventId, new ArrayList<>());
    }

    public void addComment(String moodEventId, CommentItem comment) {
        List<CommentItem> list = commentStorage.getOrDefault(moodEventId, new ArrayList<>());
        list.add(comment);
        commentStorage.put(moodEventId, list);
    }
}

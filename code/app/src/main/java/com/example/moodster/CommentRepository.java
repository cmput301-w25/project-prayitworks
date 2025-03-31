package com.example.moodster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CommentRepository class represents a singleton repository that manages CommentItem objects
 * within the Moodster app. It provides storage for comments associated with mood events, allowing
 * retrieval and addition of comments.
 */
public class CommentRepository {

    private static CommentRepository instance;

    private final Map<String, List<CommentItem>> commentStorage = new HashMap<>();

    private CommentRepository() {}

    /**
     * Returns the singleton instance of CommentRepository.
     *
     * @return
     *      the single instance of CommentRepository
     */
    public static CommentRepository getInstance() {
        if (instance == null) {
            instance = new CommentRepository();
        }
        return instance;
    }

    /**
     * Retrieves the list of comments associated with a mood event.
     *
     * @param moodEventId
     *      the unique ID of the mood event
     * @return
     *      a list of CommentItem objects for the specified mood event, or an empty list if none exist
     */
    public List<CommentItem> getCommentsForMood(String moodEventId) {
        return commentStorage.getOrDefault(moodEventId, new ArrayList<>());
    }

    /**
     * Adds a comment to the specified mood event.
     *
     * @param moodEventId
     *      the unique ID of the mood event
     * @param comment
     *      the CommentItem object to add
     */
    public void addComment(String moodEventId, CommentItem comment) {
        List<CommentItem> list = commentStorage.getOrDefault(moodEventId, new ArrayList<>());
        list.add(comment);
        commentStorage.put(moodEventId, list);
    }
}

package com.example.moodster;

import com.google.firebase.Timestamp;

/**
 * The CommentItem class represents a simple data model used in the Moodster application. This class
 * encapsulates the data for a comment, including the username of the commenter, the comment text,
 * and the timestamp of the comment creation.
 */
public class CommentItem {
    private final String username;
    private final String commentText;
    private final Timestamp timestamp;

    /**
     * Constructs a new CommentItem with the specified username, comment text, and timestamp.
     *
     * @param username
     *      the username of the person who made the comment
     * @param commentText
     *      the text content of the comment
     * @param timestamp
     *      the timestamp when the comment was created
     */
    public CommentItem(String username, String commentText, Timestamp timestamp) {
        this.username = username;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    /**
     * Returns the username of the person who made the comment.
     *
     * @return
     *      the commenter's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the text content of the comment.
     *
     * @return
     *      the comment text
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * Returns the timestamp of when the comment was created.
     *
     * @return
     *      the timestamp of the comment
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }
}

package com.example.moodster;

import com.google.firebase.Timestamp;

public class CommentItem {
    private final String username;
    private final String commentText;
    private final Timestamp timestamp;

    public CommentItem(String username, String commentText, Timestamp timestamp) {
        this.username = username;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getCommentText() {
        return commentText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}

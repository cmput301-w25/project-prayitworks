package com.example.moodster;

public class SearchUser {
    private String username;
    private String displayName;

    public SearchUser() {}

    public SearchUser(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
}

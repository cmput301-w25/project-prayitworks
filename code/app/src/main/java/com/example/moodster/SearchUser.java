package com.example.moodster;

/**
 * The SearchUser class represents a simple data model used to encapsulate basic user information
 * for search functionality and user listings.
 */
public class SearchUser {
    private String username;
    private String displayName;

    public SearchUser() {}

    /**
     * SearchUser Constructor
     *
     * @param username
     *      the unique username of the user
     * @param displayName
     *      the display name of the user
     */
    public SearchUser(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
}

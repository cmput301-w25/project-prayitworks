package com.example.moodster;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private FriendListAdapter adapter;
    private TextView tabFriends, tabRequests;
    private FirebaseFirestore db;
    private String currentUsername;
    private List<SearchUser> followingList = new ArrayList<>();
    private List<SearchUser> followersList = new ArrayList<>();
    private boolean showingFollowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        db = FirebaseFirestore.getInstance();
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        tabFriends = findViewById(R.id.tabFriends);
        tabRequests = findViewById(R.id.tabRequests);

        tabFriends.setText("Following");
        tabRequests.setText("Followers");

        // setting up tabs for followers and following
        setActiveTab(true);

        tabFriends.setOnClickListener(v -> {
            setActiveTab(true);
            updateAdapter(true);
        });
        tabRequests.setOnClickListener(v -> {
            setActiveTab(false);
            updateAdapter(false);
        });

        // getting and displaying following and followers lists
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            currentUsername = doc.getString("username");

                            // following and followers lists.
                            List<String> followingUsernames = (List<String>) doc.get("following");
                            List<String> followersUsernames = (List<String>) doc.get("followers");

                            // load followers and foollowing lists
                            if (followingUsernames != null && !followingUsernames.isEmpty()) {
                                loadFriendDetails(followingUsernames, true);
                            } else {
                                followingList.clear();
                            }
                            if (followersUsernames != null && !followersUsernames.isEmpty()) {
                                loadFriendDetails(followersUsernames, false);
                            } else {
                                followersList.clear();
                            }
                            // update the following list.
                            updateAdapter(true);
                        } else {
                            Toast.makeText(this, "Users not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show());
        }
    }

    // Loading friend details for a given list of usernames.
    private void loadFriendDetails(List<String> usernames, boolean isFollowing) {
        db.collection("Users")
                .whereIn("username", usernames)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isFollowing) {
                        followingList.clear();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            SearchUser friend = doc.toObject(SearchUser.class);
                            followingList.add(friend);
                        }
                    } else {
                        followersList.clear();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            SearchUser friend = doc.toObject(SearchUser.class);
                            followersList.add(friend);
                        }
                    }
                    // If the current tab matches, update the adapter.
                    if (isFollowing && showingFollowing) {
                        updateAdapter(true);
                    } else if (!isFollowing && !showingFollowing) {
                        updateAdapter(false);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading friend details", Toast.LENGTH_SHORT).show());
    }

    // Update RecyclerView adapter based on selected tab.
    private void updateAdapter(boolean showFollowing) {
        showingFollowing = showFollowing;
        if (showFollowing) {
            adapter = new FriendListAdapter(followingList);
        } else {
            adapter = new FriendListAdapter(followersList);
        }
        recyclerUsers.setAdapter(adapter);
    }

    // Update tab to following or followers
    private void setActiveTab(boolean followingActive) {
        if (followingActive) {
            // Following
            tabFriends.setBackgroundResource(R.drawable.bg_filter_bar_rounded);
            tabRequests.setBackgroundResource(R.drawable.bg_filter_bar);
        } else {
            // Followers
            tabFriends.setBackgroundResource(R.drawable.bg_filter_bar);
            tabRequests.setBackgroundResource(R.drawable.bg_filter_bar_rounded);
        }
    }
}

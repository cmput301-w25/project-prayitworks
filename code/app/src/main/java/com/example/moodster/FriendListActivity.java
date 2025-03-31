package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * The FriendListActivity presents a tabbed interface to show both the list of users the current user is following
 * and its followers. It also includes a search bar to filter the friend list.
 */
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

        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("My Friends");
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(FriendListActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.menu_profile) {
                        startActivity(new Intent(FriendListActivity.this, EditProfileActivity.class));
                        return true;
                    } else if (id == R.id.menu_logout) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(FriendListActivity.this, LoginActivity.class));
                        finish();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        } else {
            Toast.makeText(this, "Header menu icon not found. Check layout.", Toast.LENGTH_SHORT).show();
        }

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // --- Bottom Navigation Setup ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> startActivity(new Intent(FriendListActivity.this, HomeActivity.class).putExtra("username", currentUsername)));
        btnSearch.setOnClickListener(v -> startActivity(new Intent(FriendListActivity.this, SearchUsersActivity.class).putExtra("username", currentUsername)));
        btnAdd.setOnClickListener(v -> startActivity(new Intent(FriendListActivity.this, AddMoodActivity.class).putExtra("username", currentUsername)));
        btnCalendar.setOnClickListener(v -> startActivity(new Intent(FriendListActivity.this, MoodHistoryActivity.class).putExtra("username", currentUsername)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(FriendListActivity.this, EditProfileActivity.class).putExtra("username", currentUsername)));
        // --- End Bottom Navigation ---

        db = FirebaseFirestore.getInstance();
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        tabFriends = findViewById(R.id.tabFriends);
        tabRequests = findViewById(R.id.tabRequests);

        tabFriends.setText("Following");
        tabRequests.setText("Followers");

        setActiveTab(true);

        tabFriends.setOnClickListener(v -> {
            setActiveTab(true);
            updateAdapter(true);
        });

        tabRequests.setOnClickListener(v -> {
            setActiveTab(false);
            updateAdapter(false);
        });

        // ✅ Search bar logic
        EditText editSearchFriends = findViewById(R.id.editSearchFriends);
        ImageView iconSearch = findViewById(R.id.iconSearch);

        iconSearch.setOnClickListener(v -> {
            String query = editSearchFriends.getText().toString().trim().toLowerCase();
            List<SearchUser> sourceList = showingFollowing ? followingList : followersList;
            List<SearchUser> filtered = new ArrayList<>();

            for (SearchUser user : sourceList) {
                if (user.getUsername().toLowerCase().contains(query) ||
                        user.getDisplayName().toLowerCase().contains(query)) {
                    filtered.add(user);
                }
            }

            adapter = new FriendListAdapter(filtered);
            recyclerUsers.setAdapter(adapter);
        });

        // Load friends
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

                            List<String> followingUsernames = (List<String>) doc.get("following");
                            List<String> followersUsernames = (List<String>) doc.get("followers");

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

                            updateAdapter(true);
                        } else {
                            Toast.makeText(this, "Users not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Loads the friend details for the given list of usernames.
     *
     * <p>This method queries Firebase for users whose usernames are included in the provided list.
     * The retrieved user details are added to either the followingList or followersList based on the
     * isFollowing flag. After loading, if the currently displayed tab matches the type loaded, the
     * adapter is updated.</p>
     *
     * @param usernames
     *      the list of usernames for which to load friend details
     * @param isFollowing
     *      if true, the list represents users that the current user is following; if false, users who are following the current user
     */
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
                    if (isFollowing && showingFollowing) {
                        updateAdapter(true);
                    } else if (!isFollowing && !showingFollowing) {
                        updateAdapter(false);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading friend details", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the RecyclerView adapter with either the following or followers list.
     *
     * <p>This method sets the adapter for the RecyclerView based on whether the "Following" tab or "Followers"
     * tab is active. It also updates the internal state variable to reflect the currently displayed list.</p>
     *
     * @param showFollowing
     *      if true, the adapter is updated with the list of following users; otherwise, with followers
     */
    private void updateAdapter(boolean showFollowing) {
        showingFollowing = showFollowing;
        adapter = new FriendListAdapter(showingFollowing ? followingList : followersList);
        recyclerUsers.setAdapter(adapter);
    }

    private void setActiveTab(boolean followingActive) {
        if (followingActive) {
            tabFriends.setBackgroundResource(R.drawable.bg_filter_bar_rounded);
            tabRequests.setBackgroundResource(R.drawable.bg_filter_bar);
        } else {
            tabFriends.setBackgroundResource(R.drawable.bg_filter_bar);
            tabRequests.setBackgroundResource(R.drawable.bg_filter_bar_rounded);
        }
    }
}

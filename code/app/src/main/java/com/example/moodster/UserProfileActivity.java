package com.example.moodster;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvDisplayName, tvUsername, tvEmail, tvFollowers, tvFollowing, tvMoodCount;
    private ImageView ivAvatar;
    private Button btnFriendAction;
    private FirebaseFirestore db;
    private String targetUsername;   // The username of the profile being viewed
    private String currentUsername;  // The logged-in user's username
    private MoodEventViewModel moodEventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_view);

        db = FirebaseFirestore.getInstance();

        // 1) Set up Custom Header
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("User Profile");
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(UserProfileActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // 2) Retrieve the target username (and displayName) from the intent
        Intent intent = getIntent();
        targetUsername = intent.getStringExtra("username");
        String initialDisplayName = intent.getStringExtra("displayName");

        // 3) Set up the RecyclerView & its adapter
        RecyclerView recyclerUserMoods = findViewById(R.id.recyclerUserMoods);
        recyclerUserMoods.setLayoutManager(new LinearLayoutManager(this));

        UserMoodsAdapter userMoodsAdapter = new UserMoodsAdapter();
        recyclerUserMoods.setAdapter(userMoodsAdapter);

        // 4) Call fetchTargetUserMoods(...) so it has a valid username
        fetchTargetUserMoods(targetUsername, userMoodsAdapter);

        // 5) Bottom Navigation
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, HomeActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });
        btnSearch.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, SearchUsersActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, AddMoodActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });
        btnCalendar.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, MoodHistoryActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });
        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            i.putExtra("username", currentUsername);
            startActivity(i);
        });

        // 6) Initialize Profile UI Elements
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername    = findViewById(R.id.tv_username);
        tvEmail       = findViewById(R.id.tv_email);
        tvFollowers   = findViewById(R.id.tv_followers);
        tvFollowing   = findViewById(R.id.tv_following);
        tvMoodCount   = findViewById(R.id.tv_mood_count);
        ivAvatar      = findViewById(R.id.iv_avatar);
        btnFriendAction = findViewById(R.id.btn_friend_action);

        // 7) Set partial data right away
        if (targetUsername != null) {
            tvUsername.setText("@" + targetUsername);
        }
        if (initialDisplayName != null) {
            tvDisplayName.setText(initialDisplayName);
        }

        // 8) Dynamically update mood count for the *current* user, if you want
        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            TextView tvTotalMoods = findViewById(R.id.tv_total_moods);
            if (tvTotalMoods != null) {
                tvTotalMoods.setText("You’ve logged " + moodList.size() + " moods.");
            }
        });

        // 9) Fetch Additional Details from Firestore for the target user’s doc
        if (targetUsername != null) {
            db.collection("Users").document(targetUsername).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String displayName = documentSnapshot.getString("displayName");
                            if (displayName != null) {
                                tvDisplayName.setText(displayName);
                            }
                            String email = documentSnapshot.getString("email");
                            if (email != null && !email.isEmpty()) {
                                tvEmail.setText(email);
                                tvEmail.setVisibility(TextView.VISIBLE);
                            }
                            List<String> followersList = (List<String>) documentSnapshot.get("followers");
                            int followerCount = (followersList != null) ? followersList.size() : 0;
                            tvFollowers.setText("Followers: " + followerCount);

                            List<String> followingList = (List<String>) documentSnapshot.get("following");
                            int followingCount = (followingList != null) ? followingList.size() : 0;
                            tvFollowing.setText("Following: " + followingCount);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(UserProfileActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show());
        }

        // 10) Friend Action Button
        btnFriendAction.setText("Send Friend Request");
        btnFriendAction.setOnClickListener(v -> confirmAndSendFriendRequest());

        ivAvatar.setOnClickListener(v -> {
            // e.g., show a larger version of the avatar or a change photo dialog
        });
    }

    private void confirmAndSendFriendRequest() {
        new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle("Follow Request")
                .setMessage("Do you want to send a follow request to " + tvDisplayName.getText() + "?")
                .setPositiveButton("Yes", (dialog, which) -> sendFriendRequest())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendFriendRequest() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentEmail = firebaseUser.getEmail();
        db.collection("Users")
                .whereEqualTo("email", currentEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        currentUsername = querySnapshot.getDocuments().get(0).getString("username");
                        db.collection("Users").document(targetUsername)
                                .update("followRequests", FieldValue.arrayUnion(currentUsername))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UserProfileActivity.this, "Follow request sent.", Toast.LENGTH_SHORT).show();
                                    btnFriendAction.setText("Request Sent");
                                    btnFriendAction.setEnabled(false);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(UserProfileActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Current user not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching current user", Toast.LENGTH_SHORT).show());
    }

    /**
     * Pull the user’s mood IDs from Firestore, then fetch each mood doc in 'MoodEvents'
     */
    private void fetchTargetUserMoods(String username, UserMoodsAdapter adapter) {
        if (username == null || username.isEmpty()) return;

        db.collection("Users")
                .document(username)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        // No user doc found
                        return;
                    }

                    List<String> moodIds = (List<String>) userDoc.get("MoodEventIds");
                    if (moodIds == null || moodIds.isEmpty()) {
                        // This user has no moods
                        return;
                    }

                    List<MoodEvent> resultList = new ArrayList<>();
                    for (String moodId : moodIds) {
                        db.collection("MoodEvents")
                                .document(moodId)
                                .get()
                                .addOnSuccessListener(moodDoc -> {
                                    if (moodDoc.exists()) {
                                        try {
                                            MoodEvent mood = new MoodEvent(
                                                    moodDoc.getString("id"),
                                                    moodDoc.getTimestamp("createdAt"),
                                                    moodDoc.getString("emotionalState"),
                                                    moodDoc.getString("trigger"),
                                                    moodDoc.getString("socialSituation"),
                                                    moodDoc.getString("explanation"),
                                                    null, // ignoring image Uri for now
                                                    moodDoc.getDouble("latitude") != null ? moodDoc.getDouble("latitude") : 0.0,
                                                    moodDoc.getDouble("longitude") != null ? moodDoc.getDouble("longitude") : 0.0,
                                                    moodDoc.getBoolean("isPublic") != null ? moodDoc.getBoolean("isPublic") : true
                                            );
                                            resultList.add(mood);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // If we've processed them all, update the adapter
                                    if (resultList.size() == moodIds.size()) {
                                        adapter.setMoods(resultList);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // handle error if needed
                });
    }
}
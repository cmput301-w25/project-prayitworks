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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * The UserProfileActivity class displays a user's profile information including display name,
 * username, email, follower count, following count, and mood count. It also allows the current user
 * to send a follow request to the profile owner.
 */
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

        // --- Set up Custom Header (matches other activities) ---
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

        // --- Set up Bottom Navigation ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, SearchUsersActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, AddMoodActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, MoodHistoryActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        // --- End Bottom Navigation Setup ---


        // --- Initialize Profile UI Elements ---
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvFollowers = findViewById(R.id.tv_followers);
        tvFollowing = findViewById(R.id.tv_following);
        tvMoodCount = findViewById(R.id.tv_mood_count);
        ivAvatar = findViewById(R.id.iv_avatar);
        btnFriendAction = findViewById(R.id.btn_friend_action);

        // --- Retrieve the target username and partial data passed from UserAdapter ---
        Intent intent = getIntent();
        targetUsername = intent.getStringExtra("username");
        String initialDisplayName = intent.getStringExtra("displayName");

        // Set partial data right away
        if (targetUsername != null) {
            tvUsername.setText("@" + targetUsername);
        }
        if (initialDisplayName != null) {
            tvDisplayName.setText(initialDisplayName);
        }

        // Dynamically update mood count
        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            TextView tvMoodCount = findViewById(R.id.tv_total_moods);
            if (tvMoodCount != null) {
                tvMoodCount.setText("You’ve logged " + moodList.size() + " moods.");
            }
        });

        // --- Fetch Additional Details from Firestore ---
        if (targetUsername != null) {
            db.collection("Users").document(targetUsername).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Update UI with real data from Firestore
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

                            // For mood count, if you store them in a "Moods" collection or a user field
                            // int moodCount = ...
                            // tvMoodCount.setText(moodCount + " moods logged");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(UserProfileActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show());
        }

        // --- Set up the Friend Action Button (consistent with other areas) ---
        btnFriendAction.setText("Send Friend Request");
        btnFriendAction.setOnClickListener(v -> confirmAndSendFriendRequest());

        // Optional: Avatar click logic if needed
        ivAvatar.setOnClickListener(v -> {
            // e.g., show a larger version of the avatar or a change photo dialog
        });


    }

    /**
     * Displays a confirmation dialog to send a friend request.
     *
     * <p>This method shows an AlertDialog asking the user to confirm if they want to send a follow request
     * to the profile owner.</p>
     */
    private void confirmAndSendFriendRequest() {
        new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle("Follow Request")
                .setMessage("Do you want to send a follow request to " + tvDisplayName.getText() + "?")
                .setPositiveButton("Yes", (dialog, which) -> sendFriendRequest())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Sends a follow request to the user whose profile is being viewed.
     *
     * <p>This method retrieves the current user's username based on their email, and then updates the target user's
     * Firebase document to add the current user to the followRequests array. Upon success, it disables the friend action
     * button and updates its text to indicate that the request has been sent.</p>
     */
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
                        // Add the current user to the target's followRequests array
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
}

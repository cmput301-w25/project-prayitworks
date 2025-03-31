package com.example.moodster;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The EditProfileActivity allows a user to view their profile details and update fields such as display name
 * and username. It retrieves user information from Firebase and updates the profile data based off user actions
 */
public class EditProfileActivity extends AppCompatActivity {

    // Header & Profile UI
    private ImageView backButton;
    private TextView tvDisplayName, tvUsername, tvEmail;
    private Button btnChangeName, btnChangeUsername;

    // Bottom Navigation (ImageButtons in the layout)
    private ImageButton btnHome, btnSearch, btnAdd, btnCalendar, btnProfile;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Mood ViewModel
    private MoodEventViewModel moodEventViewModel;

    // Current username
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Retrieve username from Intent
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set username in the MoodEventViewModel
        MoodEventViewModel.getInstance().setUsername(currentUsername);


        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("My Profile");

        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(EditProfileActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.menu_profile) {
                        startActivity(new Intent(EditProfileActivity.this, EditProfileActivity.class));
                        return true;
                    } else if (id == R.id.menu_logout) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
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
        // --- End Header Setup ---

        // Bind views from layout
        backButton = findViewById(R.id.back_button);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnChangeName = findViewById(R.id.change_name);
        btnChangeUsername = findViewById(R.id.change_username);

        // Load current user info from Firestore
        loadUserInfo();

        // Dynamically update mood count
        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            TextView tvMoodCount = findViewById(R.id.tv_total_moods);
            if (tvMoodCount != null) {
                tvMoodCount.setText("You’ve logged " + moodList.size() + " moods.");
            }
        });

        // --- Bottom Navigation Setup ---
        btnHome = findViewById(R.id.btn_home);
        btnSearch = findViewById(R.id.btn_search);
        btnAdd = findViewById(R.id.btn_add);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
        });
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, MapHandlerActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, AddMoodActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, MoodHistoryActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnProfile.setOnClickListener(v ->
                Toast.makeText(EditProfileActivity.this, "Already on Profile", Toast.LENGTH_SHORT).show()
        );
// --- End Bottom Navigation Setup ---

    }

    /**
     * Loads user profile information from Firestore.
     *
     * <p>This method gets the current user's email from Firebase Authentication and queries the "Users"
     * collection in Firestore for the corresponding document. If found, it updates the UI components with the
     * retrieved data.</p>
     */
    private void loadUserInfo() {
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            String displayName = doc.getString("displayName");
                            String username = doc.getString("username");
                            String userEmail = doc.getString("email");

                            tvDisplayName.setText(displayName != null ? displayName : "N/A");
                            tvUsername.setText(currentUsername); // or username if you prefer
                            tvEmail.setText(userEmail != null ? userEmail : "N/A");
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Your Name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateProfileField("displayName", newName, "Name updated successfully");
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showChangeUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Your Username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateProfileField("username", newUsername, "Username updated successfully");
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Updates a specified profile field in Firestore and refreshes the user information.
     *
     * <p>This generic helper method queries Firestore for the current user's document using their email.
     * It then updates the specified field with the provided new value. Upon a successful update,
     * a success message is displayed and loadUserInfo() is called to refresh the UI.</p>
     *
     * @param field
     *      the name of the profile field to update
     * @param newValue
     *      the new value to set for the specified field
     * @param successMessage
     *      the message to display upon a successful update
     */
    private void updateProfileField(String field, String newValue, String successMessage) {
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            String docId = doc.getId();
                            db.collection("Users")
                                    .document(docId)
                                    .update(field, newValue)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(EditProfileActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                                        loadUserInfo(); // Refresh displayed user info
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(EditProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        } else {
                            Toast.makeText(EditProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(EditProfileActivity.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}

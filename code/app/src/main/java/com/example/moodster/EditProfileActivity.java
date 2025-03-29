package com.example.moodster;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    // Views for the profile info
    private ImageView backButton;
    private TextView tvDisplayName, tvUsername, tvEmail;
    private Button btnChangeName, btnChangeUsername;

    // Firebase instances
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Bottom navigation buttons
    private ImageButton btnHome, btnSearch, btnAdd, btnCalendar, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views from the XML layout
        backButton = findViewById(R.id.back_button);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnChangeName = findViewById(R.id.change_name);
        btnChangeUsername = findViewById(R.id.change_username);

        // Bind bottom navigation buttons
        btnHome = findViewById(R.id.btn_home);
        btnSearch = findViewById(R.id.btn_search);
        btnAdd = findViewById(R.id.btn_add);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnProfile = findViewById(R.id.btn_profile);

        // Back button: finish activity
        backButton.setOnClickListener(v -> finish());

        // Load current user info from Firestore
        loadUserInfo();

        // Set up action button listeners
        btnChangeName.setOnClickListener(v -> showChangeNameDialog());
        btnChangeUsername.setOnClickListener(v -> showChangeUsernameDialog());

        // Bottom navigation listeners, consistent with other screens
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
            finish();
        });
        btnSearch.setOnClickListener(v -> startActivity(new Intent(EditProfileActivity.this, MapHandlerActivity.class)));
        btnAdd.setOnClickListener(v -> startActivity(new Intent(EditProfileActivity.this, AddMoodActivity.class)));
        btnCalendar.setOnClickListener(v -> startActivity(new Intent(EditProfileActivity.this, MoodHistoryActivity.class)));
        btnProfile.setOnClickListener(v ->
                Toast.makeText(EditProfileActivity.this, "Already on Profile", Toast.LENGTH_SHORT).show()
        );
    }

    // Loads the current user’s profile information from Firestore
    private void loadUserInfo() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
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
                            tvUsername.setText(username != null ? username : "N/A");
                            tvEmail.setText(userEmail != null ? userEmail : "N/A");
                        } else {
                            Toast.makeText(EditProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(EditProfileActivity.this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    // Opens a dialog to change the user's display name
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
                Toast.makeText(EditProfileActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Opens a dialog to change the user's username
    private void showChangeUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Your Username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                // Note: Changing username might require extra handling if used as a document ID.
                updateProfileField("username", newUsername, "Username updated successfully");
            } else {
                Toast.makeText(EditProfileActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Updates a profile field in Firestore and refreshes the UI upon success
    private void updateProfileField(String field, String newValue, String successMessage) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
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
                                        loadUserInfo(); // Refresh the displayed user info
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

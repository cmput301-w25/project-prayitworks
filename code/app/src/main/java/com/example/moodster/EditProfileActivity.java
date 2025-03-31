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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView tvDisplayName, tvUsername, tvEmail;
    private Button btnChangeName, btnChangeUsername;
    private ImageButton btnHome, btnSearch, btnAdd, btnCalendar, btnProfile;

    private FirebaseFirestore db;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        db = FirebaseFirestore.getInstance();

        // ✅ Get current username from intent
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // ✅ Set username in ViewModel
        MoodEventViewModel.getInstance().setUsername(currentUsername);

        // Bind views
        backButton = findViewById(R.id.back_button);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnChangeName = findViewById(R.id.change_name);
        btnChangeUsername = findViewById(R.id.change_username);

        btnHome = findViewById(R.id.btn_home);
        btnSearch = findViewById(R.id.btn_search);
        btnAdd = findViewById(R.id.btn_add);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnProfile = findViewById(R.id.btn_profile);

        backButton.setOnClickListener(v -> finish());

        // Load profile
        loadUserInfo();

        // Change name / username
        btnChangeName.setOnClickListener(v -> showChangeNameDialog());
        btnChangeUsername.setOnClickListener(v -> showChangeUsernameDialog());

        // Bottom nav
        btnHome.setOnClickListener(v -> startActivityWithUsername(HomeActivity.class));
        btnSearch.setOnClickListener(v -> startActivityWithUsername(MapHandlerActivity.class));
        btnAdd.setOnClickListener(v -> startActivityWithUsername(AddMoodActivity.class));
        btnCalendar.setOnClickListener(v -> startActivityWithUsername(MoodHistoryActivity.class));
        btnProfile.setOnClickListener(v -> Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show());
    }

    private void startActivityWithUsername(Class<?> cls) {
        Intent intent = new Intent(EditProfileActivity.this, cls);
        intent.putExtra("username", currentUsername);
        startActivity(intent);
    }

    private void loadUserInfo() {
        db.collection("Users")
                .document(currentUsername)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String displayName = doc.getString("displayName");
                        String email = doc.getString("email");

                        tvDisplayName.setText(displayName != null ? displayName : "N/A");
                        tvUsername.setText(currentUsername);
                        tvEmail.setText(email != null ? email : "N/A");
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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
                // 🔥 TODO: Rename user document ID if needed
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateProfileField(String field, String newValue, String successMessage) {
        db.collection("Users")
                .document(currentUsername)
                .update(field, newValue)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                    loadUserInfo();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

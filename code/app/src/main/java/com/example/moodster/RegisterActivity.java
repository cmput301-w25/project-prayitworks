package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The RegisterActivity class handles user registration by collecting user input for email, username,
 * display name, password, and backup password. It validates the input, checks for username uniqueness
 * in Firebase and creates a new user account using Firebase Authentication, and saves the user's
 * profile details to Firebase.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etDisplayName, etPassword, etConfirmPassword, etBackupPassword;
    private TextView tvUsernameError, tvLogin;
    private MaterialButton btnRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etDisplayName = findViewById(R.id.et_display_name);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etBackupPassword = findViewById(R.id.et_backup_password);
        tvUsernameError = findViewById(R.id.tv_username_error);
        tvLogin = findViewById(R.id.tv_login);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> attemptRegistration());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Attempts to register a new user by validating input fields and checking for username uniqueness.
     *
     * <p>If all fields are filled and the passwords match, the method queries Firebase to ensure the username
     * is not already taken. If the username is unique, it registers the user with Firebase.</p>
     */
    private void attemptRegistration() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String backupPassword = etBackupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(displayName) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(backupPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        tvUsernameError.setVisibility(View.VISIBLE);
                    } else {
                        tvUsernameError.setVisibility(View.GONE);
                        registerUserWithFirebase(email, password, backupPassword, username, displayName);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Registers a new user using Firebase Authentication and saves the user profile to Firebase.
     *
     * <p>This method creates a new user with the provided email and password, then hashes the backup password
     * using SHA-256. The user profile, including display name, email, username, and empty lists for followers,
     * following, and mood events, is then stored in Firebase.</p>
     *
     * @param email
     *      the user's email address
     * @param password
     *      the user's password
     * @param backupPassword
     *      the backup password for account recovery
     * @param username
     *      the desired username
     * @param displayName
     *      the user's display name
     */
    private void registerUserWithFirebase(String email, String password, String backupPassword, String username, String displayName) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        String hashedBackup = hashSHA256(backupPassword);

                        Map<String, Object> profile = new HashMap<>();
                        profile.put("displayName", displayName);
                        profile.put("email", email);
                        profile.put("username", username);
                        profile.put("followers", new ArrayList<>());
                        profile.put("following", new ArrayList<>());
                        profile.put("followersCount", 0);
                        profile.put("followingCount", 0);
                        profile.put("MoodEventIds", new ArrayList<>());
                        profile.put("backupPassword", hashedBackup);
                        profile.put("followRequests", new ArrayList<>());

                        db.collection("Users").document(username)
                                .set(profile)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firebase Auth failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Computes the SHA-256 hash of the given input string.
     *
     * <p>This method takes an input string, hashes it using the SHA-256 algorithm, and returns its
     * hex representation. This hash is used to verify the backup password against the stored hash
     * in Firebase.</p>
     *
     * @param input
     *      the string to hash
     * @return
     *      the SHA-256 hash of the input in hex
     * @throws RuntimeException
     *      if an error occurs during hashing
     */
    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}

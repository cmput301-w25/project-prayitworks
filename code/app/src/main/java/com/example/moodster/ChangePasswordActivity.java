package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The ChangePasswordActivity class implements an activity that allows a user to change their
 * password as part of the password reset process initiated in ForgotPasswordActivity. The activity
 * validates user input and uses Firebase Authentication to update the password. After a successful
 * update, the user is signed out and redirected to the LoginActivity,
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private MaterialButton btnChangePassword;
    private ImageView btnBack;

    private FirebaseAuth auth;
    private String email; // Comes from ForgotPasswordActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_pass);

        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_update);
        btnBack = findViewById(R.id.btn_back);

        auth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");

        btnBack.setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            auth.signOut();
                            Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}

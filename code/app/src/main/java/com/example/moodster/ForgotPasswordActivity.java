package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etUsernameOrEmail, etBackupPassword;
    private MaterialButton btnContinue;
    private ImageView btnBack;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        etUsernameOrEmail = findViewById(R.id.et_username_or_email);
        etBackupPassword = findViewById(R.id.et_backup_password);
        btnContinue = findViewById(R.id.btn_continue);
        btnBack = findViewById(R.id.btn_back);

        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String input = etUsernameOrEmail.getText().toString().trim();
            String backup = etBackupPassword.getText().toString().trim();

            if (TextUtils.isEmpty(input) || TextUtils.isEmpty(backup)) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedBackup = hashSHA256(backup);

            if (input.contains("@")) {
                db.collection("Users")
                        .whereEqualTo("email", input)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                String email = snapshot.getDocuments().get(0).getString("email");
                                String storedBackup = snapshot.getDocuments().get(0).getString("backupPassword");

                                if (hashedBackup.equals(storedBackup)) {
                                    goToChangePassword(email);
                                } else {
                                    Toast.makeText(this, "Backup password incorrect", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                db.collection("Users")
                        .document(input)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String email = doc.getString("email");
                                String storedBackup = doc.getString("backupPassword");

                                if (hashedBackup.equals(storedBackup)) {
                                    goToChangePassword(email);
                                } else {
                                    Toast.makeText(this, "Backup password incorrect", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void goToChangePassword(String email) {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

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

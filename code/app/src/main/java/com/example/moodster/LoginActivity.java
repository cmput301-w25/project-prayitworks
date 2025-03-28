package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ImageView btnBack;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);
        btnBack = findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnBack.setOnClickListener(v -> finish());

        // 👉 Launch ForgotPasswordActivity instead
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");

                        if (!TextUtils.isEmpty(email)) {
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> {
                                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Email not found for this username", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error finding username: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

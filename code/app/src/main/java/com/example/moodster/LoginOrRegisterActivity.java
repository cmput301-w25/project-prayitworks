package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginOrRegisterActivity extends AppCompatActivity {

    private Button loginButton, registerButton;
    private TextView guestText; // Optional: for a "Continue as Guest" option

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginorreg_screen);

        // Bind views
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        // Set up button click listeners
        loginButton.setOnClickListener(v -> {
            // Navigate to LoginActivity
            Intent intent = new Intent(LoginOrRegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            // Navigate to RegisterActivity
            Intent intent = new Intent(LoginOrRegisterActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
}
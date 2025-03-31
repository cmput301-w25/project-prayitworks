package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    // Delay duration in milliseconds (e.g., 3000ms = 3 seconds)
    private static final long SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_opening);

        // Delay transition to the LoginOrRegisterActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginOrRegisterActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
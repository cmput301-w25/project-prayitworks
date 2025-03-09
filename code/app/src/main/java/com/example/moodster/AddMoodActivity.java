package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AddMoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood);

        Button btnAngry = findViewById(R.id.btn_angry);
        Button btnSad = findViewById(R.id.btn_sad);
        Button btnFear = findViewById(R.id.btn_fear);
        Button btnShame = findViewById(R.id.btn_shame);
        Button btnHappy = findViewById(R.id.btn_happy);

        ImageButton btnViewMoodHistory = findViewById(R.id.btn_calendar);


        btnAngry.setOnClickListener(view -> openMoodActivity("Anger"));

        btnSad.setOnClickListener(view -> openMoodActivity("Sadness"));

        btnFear.setOnClickListener(view -> openMoodActivity("Fear"));

        btnShame.setOnClickListener(view -> openMoodActivity("Shame"));

        btnHappy.setOnClickListener(view -> openMoodActivity("Happiness"));

        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(AddMoodActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void openMoodActivity(String mood) {
        Intent intent = new Intent(AddMoodActivity.this, MoodActivity.class);
        intent.putExtra("mood", mood);
        startActivity(intent);
    }
}
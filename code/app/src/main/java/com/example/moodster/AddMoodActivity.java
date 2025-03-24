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
        Button btnConfusion = findViewById(R.id.btn_confusion);
        Button btnDisgust = findViewById(R.id.btn_disgust);
        Button btnSad = findViewById(R.id.btn_sad);
        Button btnFear = findViewById(R.id.btn_fear);
        Button btnShame = findViewById(R.id.btn_shame);
        Button btnHappy = findViewById(R.id.btn_happy);
        Button btnSurprise = findViewById(R.id.btn_surprise);

        ImageButton btnViewMoodHistory = findViewById(R.id.btn_calendar);
        ImageButton btnSearch = findViewById(R.id.btn_search);

        btnAngry.setOnClickListener(view -> openMoodActivity("Anger"));
        btnConfusion.setOnClickListener(view -> openMoodActivity("Confusion"));
        btnDisgust.setOnClickListener(view -> openMoodActivity("Disgust"));
        btnSad.setOnClickListener(view -> openMoodActivity("Sadness"));
        btnFear.setOnClickListener(view -> openMoodActivity("Fear"));
        btnShame.setOnClickListener(view -> openMoodActivity("Shame"));
        btnHappy.setOnClickListener(view -> openMoodActivity("Happiness"));
        btnSurprise.setOnClickListener(view -> openMoodActivity("Surprise"));

        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(AddMoodActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(AddMoodActivity.this, MapHandlerActivity.class);
            startActivity(intent);
        });
    }

    private void openMoodActivity(String mood) {
        Intent intent = new Intent(AddMoodActivity.this, MoodActivity.class);
        intent.putExtra("mood", mood);
        startActivity(intent);
    }
}
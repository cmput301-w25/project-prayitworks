package com.example.moodster;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class MoodDetailsActivity extends AppCompatActivity {
    private TextView textViewDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_details);

        textViewDetails = findViewById(R.id.textViewDetails);

        MoodEvent moodEvent = (MoodEvent) getIntent().getSerializableExtra("mood");

        if (moodEvent != null) {
            String details = "Emotional State: " + moodEvent.getEmotionalState() +
                    "\nTrigger: " + moodEvent.getTrigger() +
                    "\nSocial Situation: " + moodEvent.getSocialSituation() +
                    "\nExplanation: " + moodEvent.getExplanation() +
                    "\nTimestamp: " + moodEvent.getCreatedAt();

            textViewDetails.setText(details);
        }
    }
}

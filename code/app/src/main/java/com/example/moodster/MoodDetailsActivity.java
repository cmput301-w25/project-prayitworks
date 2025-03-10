package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodDetailsActivity extends AppCompatActivity {
    private TextView textMoodEmoji, textMoodDateTime, textReasonValue, textTriggerValue, textSocialValue;
    private ImageView imageMoodPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_press_view_details_mood_history);

        Intent intent = getIntent();

        textMoodEmoji = findViewById(R.id.textMoodEmoji);
        textMoodDateTime = findViewById(R.id.textMoodDateTime);
        textReasonValue = findViewById(R.id.textReasonValue);
        textTriggerValue = findViewById(R.id.textTriggerValue);
        textSocialValue = findViewById(R.id.textSocialValue);
        imageMoodPhoto = findViewById(R.id.imageMoodPhoto);
        textMoodEmoji.setText(intent.getStringExtra("textMoodEmoji"));
        textMoodDateTime.setText(String.valueOf(intent.getStringExtra("textMoodDateTime")));
        textReasonValue.setText(intent.getStringExtra("textReasonValue"));
        textTriggerValue.setText(intent.getStringExtra("textTriggerValue"));
        textSocialValue.setText(intent.getStringExtra("textSocialValue"));
        imageMoodPhoto.setImageURI(intent.getParcelableExtra("imageMoodPhoto"));
    }
}

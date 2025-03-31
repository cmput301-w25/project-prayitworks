package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MoodDetailsActivity extends AppCompatActivity {

    private TextView textMoodEmoji, textMoodDateTime, textReasonValue,
            textTriggerValue, textSocialValue;
    private ImageView imageMoodPhoto;
    private MoodEventViewModel moodEventViewModel;
    private String moodId;
    private String currentUsername;
    private Button btnDeleteMood, btnEditMood, btnViewComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_press_view_details_mood_history);

        // ✅ ViewModel setup
        moodEventViewModel = MoodEventViewModel.getInstance();

        // ✅ Grab from intent
        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        moodId = intent.getStringExtra("moodId");

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        moodEventViewModel.setUsername(currentUsername);

        // ✅ Bind views
        textMoodEmoji = findViewById(R.id.textMoodEmoji);
        textMoodDateTime = findViewById(R.id.textMoodDateTime);
        textReasonValue = findViewById(R.id.textReasonValue);
        textTriggerValue = findViewById(R.id.textTriggerValue);
        textSocialValue = findViewById(R.id.textSocialValue);
        imageMoodPhoto = findViewById(R.id.imageMoodPhoto);
        btnViewComments = findViewById(R.id.viewComments);
        btnDeleteMood = findViewById(R.id.buttonDelete);
        btnEditMood = findViewById(R.id.buttonEdit);

        // ✅ Fill in data from intent
        textMoodEmoji.setText(intent.getStringExtra("textMoodEmoji"));
        textMoodDateTime.setText(String.valueOf(intent.getStringExtra("textMoodDateTime")));
        textReasonValue.setText(intent.getStringExtra("textReasonValue"));
        textTriggerValue.setText(intent.getStringExtra("textTriggerValue"));
        textSocialValue.setText(intent.getStringExtra("textSocialValue"));
        imageMoodPhoto.setImageURI(intent.getParcelableExtra("imageMoodPhoto"));

        // ✅ Delete mood
        btnDeleteMood.setOnClickListener(v -> {
            moodEventViewModel.deleteMoodEvent(moodId, new MoodEventViewModel.OnDeleteListener() {
                @Override
                public void onDeleteSuccess() {
                    Intent backToHistory = new Intent(MoodDetailsActivity.this, MoodHistoryActivity.class);
                    backToHistory.putExtra("username", currentUsername); // ✅ pass username
                    startActivity(backToHistory);
                    finish();
                }

                @Override
                public void onDeleteFailure(String errorMessage) {
                    Toast.makeText(MoodDetailsActivity.this, "Delete failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // ✅ Edit mood
        btnEditMood.setOnClickListener(v -> {
            Intent intentEdit = new Intent(MoodDetailsActivity.this, EditMoodActivity.class);
            intentEdit.putExtra("moodId", moodId);
            intentEdit.putExtra("username", currentUsername); // ✅ pass username
            startActivity(intentEdit);
        });

        // ✅ View All Comments
        btnViewComments.setOnClickListener(v -> {
            Intent intentComment = new Intent(MoodDetailsActivity.this, CommentsActivity.class);
            intentComment.putExtra("moodEventId", moodId);
            intentComment.putExtra("username", currentUsername); // ✅
            startActivity(intentComment);
        });
    }
}

package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoodDetailsActivity extends AppCompatActivity {
    private TextView textMoodEmoji, textMoodDateTime, textReasonValue,
            textTriggerValue, textSocialValue, textRecentComment1, textRecentComment2;
    private ImageView imageMoodPhoto;
    private MoodEventViewModel moodEventViewModel;
    private String moodId;
    private String currentUsername;
    private Button btnDeleteMood, btnEditMood, btnViewComments;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_press_view_details_mood_history);

        moodEventViewModel = MoodEventViewModel.getInstance();
        db = FirebaseFirestore.getInstance();

        // Grab from intent
        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        moodId = intent.getStringExtra("moodId");

        // Bind views
        textMoodEmoji = findViewById(R.id.textMoodEmoji);
        textMoodDateTime = findViewById(R.id.textMoodDateTime);
        textReasonValue = findViewById(R.id.textReasonValue);
        textTriggerValue = findViewById(R.id.textTriggerValue);
        textSocialValue = findViewById(R.id.textSocialValue);
        imageMoodPhoto = findViewById(R.id.imageMoodPhoto);
        textRecentComment1 = findViewById(R.id.textRecentComment1);
        textRecentComment2 = findViewById(R.id.textRecentComment2);
        btnViewComments = findViewById(R.id.viewComments);

        textMoodEmoji.setText(intent.getStringExtra("textMoodEmoji"));
        textMoodDateTime.setText(String.valueOf(intent.getStringExtra("textMoodDateTime")));
        textReasonValue.setText(intent.getStringExtra("textReasonValue"));
        textTriggerValue.setText(intent.getStringExtra("textTriggerValue"));
        textSocialValue.setText(intent.getStringExtra("textSocialValue"));
        imageMoodPhoto.setImageURI(intent.getParcelableExtra("imageMoodPhoto"));

        // Delete
        btnDeleteMood = findViewById(R.id.buttonDelete);
        btnDeleteMood.setOnClickListener(v -> {
            moodEventViewModel.deleteMoodEvent(moodId, new MoodEventViewModel.OnDeleteListener() {
                @Override
                public void onDeleteSuccess() {
                    startActivity(new Intent(MoodDetailsActivity.this, MoodHistoryActivity.class));
                    finish();
                }

                @Override
                public void onDeleteFailure(String errorMessage) {
                    Toast.makeText(MoodDetailsActivity.this, "Delete failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Edit
        btnEditMood = findViewById(R.id.buttonEdit);
        btnEditMood.setOnClickListener(v -> {
            Intent intentEdit = new Intent(MoodDetailsActivity.this, EditMoodActivity.class);
            intentEdit.putExtra("moodId", moodId);
            startActivity(intentEdit);
        });

        // View All Comments
        btnViewComments.setOnClickListener(v -> {
            Intent intentComment = new Intent(MoodDetailsActivity.this, CommentsActivity.class);
            intentComment.putExtra("moodEventId", moodId);
            intentComment.putExtra("username", currentUsername);
            startActivity(intentComment);
        });

        loadRecentComments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentComments();
    }

    private void loadRecentComments() {
        db.collection("Comments").document(moodId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Map<String, Object> data = documentSnapshot.getData();
                    if (data == null) return;

                    List<CommentItem> allComments = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        if (!(entry.getValue() instanceof List)) continue;
                        List<Map<String, Object>> userCommentObjects = (List<Map<String, Object>>) entry.getValue();

                        for (Map<String, Object> commentObj : userCommentObjects) {
                            String text = (String) commentObj.get("text");
                            Timestamp time = (Timestamp) commentObj.get("time");
                            if (text != null && time != null) {
                                allComments.add(new CommentItem(entry.getKey(), text, time));
                            }
                        }
                    }

                    allComments.sort((a, b) ->
                            b.getTimestamp().toDate().compareTo(a.getTimestamp().toDate()));

                    if (allComments.size() >= 1) {
                        textRecentComment1.setText("\u2022 " + allComments.get(0).getCommentText());
                    }
                    if (allComments.size() >= 2) {
                        textRecentComment2.setText("\u2022 " + allComments.get(1).getCommentText());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                });
    }
}

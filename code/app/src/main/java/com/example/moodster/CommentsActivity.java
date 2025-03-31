package com.example.moodster;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private ListView commentListView;
    private EditText inputComment;
    private Button btnPostComment;
    private CommentAdapter adapter;
    private final List<CommentItem> commentList = new ArrayList<>();

    private String currentUsername; // your username (for now just mock it)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentListView = findViewById(R.id.commentListView);
        inputComment = findViewById(R.id.inputComment);
        btnPostComment = findViewById(R.id.btnPostComment);

        // 🔧 Hardcode username for now or grab from intent
        currentUsername = "YourUsername"; // or getIntent().getStringExtra("username");

        adapter = new CommentAdapter(this, commentList, currentUsername, null);
        commentListView.setAdapter(adapter);

        btnPostComment.setOnClickListener(v -> {
            String commentText = inputComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) return;

            // Create new comment item
            CommentItem newComment = new CommentItem(currentUsername, commentText, Timestamp.now());

            // Add to top of the list
            commentList.add(0, newComment);
            adapter.notifyDataSetChanged();

            // Clear input
            inputComment.setText("");

            // Scroll to top
            commentListView.setSelection(0);
        });
    }
}

package com.example.moodster;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private ListView commentListView;
    private EditText inputComment;
    private Button btnPostComment;
    private CommentAdapter adapter;

    private List<CommentItem> commentList;

    private String currentUsername;
    private String moodEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentListView = findViewById(R.id.commentListView);
        inputComment = findViewById(R.id.inputComment);
        btnPostComment = findViewById(R.id.btnPostComment);

        currentUsername = getIntent().getStringExtra("username");
        moodEventId = getIntent().getStringExtra("moodEventId");

        // ✅ Get previously saved comments for this mood (in-memory)
        commentList = CommentRepository.getInstance().getCommentsForMood(moodEventId);

        adapter = new CommentAdapter(this, commentList, currentUsername, moodEventId);
        commentListView.setAdapter(adapter);

        btnPostComment.setOnClickListener(v -> {
            String commentText = inputComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) return;

            CommentItem newComment = new CommentItem(currentUsername, commentText, Timestamp.now());

            // ✅ Save to global in-memory store
            CommentRepository.getInstance().addComment(moodEventId, newComment);

            // ✅ Update list and UI
            commentList.add(0, newComment);
            adapter.notifyDataSetChanged();
            inputComment.setText("");
            commentListView.setSelection(0);
        });
    }
}

package com.example.moodster;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (currentUsername == null) {
            Toast.makeText(this, "Username not found!", Toast.LENGTH_SHORT).show();
            finish(); // or fallback
            return;
        }

        moodEventId = getIntent().getStringExtra("moodEventId");

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(this, commentList, currentUsername, moodEventId);
        commentListView.setAdapter(adapter);

        btnPostComment.setOnClickListener(v -> {
            String commentText = inputComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) return;

            CommentItem newComment = new CommentItem(currentUsername, commentText, Timestamp.now());
            saveCommentToFirestore(newComment);

            inputComment.setText("");
            commentListView.post(() -> commentListView.setSelection(0));
        });

        loadCommentsFromFirestore();
    }

    private void saveCommentToFirestore(CommentItem commentItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("username", commentItem.getUsername());
        commentMap.put("text", commentItem.getCommentText());
        commentMap.put("time", commentItem.getTimestamp());

        db.collection("Comments").document(moodEventId)
                .update("comments", FieldValue.arrayUnion(commentMap))
                .addOnSuccessListener(unused -> {
                    loadCommentsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Map<String, Object> newDoc = new HashMap<>();
                    List<Map<String, Object>> list = new ArrayList<>();
                    list.add(commentMap);
                    newDoc.put("comments", list);

                    db.collection("Comments").document(moodEventId)
                            .set(newDoc)
                            .addOnSuccessListener(unused2 -> {
                                loadCommentsFromFirestore();
                            })
                            .addOnFailureListener(ex ->
                                    Toast.makeText(this, "Failed to save comment", Toast.LENGTH_SHORT).show()
                            );
                });
    }

    private void loadCommentsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Comments").document(moodEventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    commentList.clear();
                    if (documentSnapshot.exists() && documentSnapshot.contains("comments")) {
                        List<Map<String, Object>> rawComments =
                                (List<Map<String, Object>>) documentSnapshot.get("comments");

                        for (Map<String, Object> c : rawComments) {
                            String username = (String) c.get("username");
                            String text = (String) c.get("text");
                            Timestamp time = (Timestamp) c.get("time");

                            if (username != null && text != null && time != null) {
                                commentList.add(new CommentItem(username, text, time));
                            }
                        }

                        sortAndRefresh();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show());
    }


    private void sortAndRefresh() {
        Collections.sort(commentList, (a, b) -> {
            boolean isAUser = a.getUsername().equals(currentUsername);
            boolean isBUser = b.getUsername().equals(currentUsername);

            if (isAUser && !isBUser) return -1;
            if (!isAUser && isBUser) return 1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        adapter.notifyDataSetChanged();
    }
}

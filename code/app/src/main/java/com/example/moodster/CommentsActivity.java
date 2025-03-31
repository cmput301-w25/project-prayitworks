package com.example.moodster;

import android.content.Intent;
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

/**
 * The CommentsActivity class retrieves the current user's username and the mood event ID from the
 * Intent. It initializes a CommentAdapter to display the list of comments, allows the posting of
 * new comments, and handles the sorting and refreshing of comments by interacting with Firebase.
 */
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

        // ✅ Validate and grab data from intent
        currentUsername = getIntent().getStringExtra("username");
        moodEventId = getIntent().getStringExtra("moodEventId");

        if (currentUsername == null || moodEventId == null) {
            Toast.makeText(this, "Missing username or mood ID!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Set username in ViewModel for consistency
        MoodEventViewModel.getInstance().setUsername(currentUsername);

        // ✅ Initialize adapter
        commentList = new ArrayList<>();
        adapter = new CommentAdapter(this, commentList, currentUsername, moodEventId);
        commentListView.setAdapter(adapter);

        // ✅ Post comment
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

    /**
     * Saves a comment to Firebase Firestore.
     *
     * <p>This method attempts to update an existing document for the mood event by adding the new comment
     * to the "comments" array. If the document does not exist, it creates a new document containing the comment.</p>
     *
     * @param commentItem
     *      the CommentItem object representing the comment to be saved
     */
    private void saveCommentToFirestore(CommentItem commentItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("username", commentItem.getUsername());
        commentMap.put("text", commentItem.getCommentText());
        commentMap.put("time", commentItem.getTimestamp());

        db.collection("Comments").document(moodEventId)
                .update("comments", FieldValue.arrayUnion(commentMap))
                .addOnSuccessListener(unused -> loadCommentsFromFirestore())
                .addOnFailureListener(e -> {
                    // Create document if it doesn't exist
                    Map<String, Object> newDoc = new HashMap<>();
                    List<Map<String, Object>> list = new ArrayList<>();
                    list.add(commentMap);
                    newDoc.put("comments", list);

                    db.collection("Comments").document(moodEventId)
                            .set(newDoc)
                            .addOnSuccessListener(unused2 -> loadCommentsFromFirestore())
                            .addOnFailureListener(ex ->
                                    Toast.makeText(this, "Failed to save comment", Toast.LENGTH_SHORT).show()
                            );
                });
    }

    /**
     * Loads comments for the mood event from Firebase Firestore.
     *
     * <p>This method retrieves the document corresponding to the mood event ID from the "Comments" collection.
     * If the document exists and contains a "comments" field, the comments are parsed and added to the local
     * comment list. The comments are then sorted and UI is refreshed.</p>
     */
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

    /**
     * Loads comments for the mood event from Firebase Firestore.
     *
     * <p>Sorts the comment list by prioritizing comments from the current user to appear at the top,
     * followed by comments from other users sorted by timestamp in descending order, then notifies
     * the adapter to refresh</p>
     */
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

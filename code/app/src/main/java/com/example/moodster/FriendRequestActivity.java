package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity {
    private RecyclerView recyclerRequests;
    private FriendRequestAdapter adapter;
    private FirebaseFirestore db;
    private String currentUsername;
    private List<String> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_recieving_follower_request);

        Button btnTabFriends = findViewById(R.id.tabFriends);
        Button btnSearchUsers = findViewById(R.id.btnSearchUsers);

        db = FirebaseFirestore.getInstance();
        recyclerRequests = findViewById(R.id.recyclerRequests);

        // Setup RecyclerView
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            currentUsername = querySnapshot.getDocuments().get(0).getString("username");
                            loadFriendRequests();
                        } else {
                            Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading username", Toast.LENGTH_SHORT).show());
        }

        btnTabFriends.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, FriendListActivity.class);
            startActivity(intent);
        });

        btnSearchUsers.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, SearchUsersActivity.class);
            startActivity(intent);
        });
    }

    private void loadFriendRequests() {
        // Get the current user's document to retrieve the followRequests field.
        db.collection("Users").document(currentUsername).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> requests = (List<String>) documentSnapshot.get("followRequests");
                        if (requests != null) {
                            requestList.clear();
                            requestList.addAll(requests);
                            adapter = new FriendRequestAdapter(requestList, currentUsername);
                            recyclerRequests.setAdapter(adapter);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(FriendRequestActivity.this, "Error loading friend requests", Toast.LENGTH_SHORT).show());
    }

}

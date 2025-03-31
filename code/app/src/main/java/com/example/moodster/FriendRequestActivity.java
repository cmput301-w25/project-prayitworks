package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("Friend Requests");
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(FriendRequestActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(FriendRequestActivity.this, EditProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(FriendRequestActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });
        // --- End Header Setup ---

        // Existing UI initialization
        Button btnTabFriends = findViewById(R.id.tabFriends);
        Button btnSearchUsers = findViewById(R.id.btnSearchUsers);

        db = FirebaseFirestore.getInstance();
        recyclerRequests = findViewById(R.id.recyclerRequests);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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

        // --- Bottom Navigation Setup ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, HomeActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, SearchUsersActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, AddMoodActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, MoodHistoryActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FriendRequestActivity.this, EditProfileActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
        // --- End Bottom Navigation Setup ---

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

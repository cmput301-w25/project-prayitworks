package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {
    private EditText editSearch;
    private RecyclerView recyclerUsers;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_for_participant_result_screen);

        // --- Setup Custom Header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set the screen title dynamically
        ((android.widget.TextView) findViewById(R.id.tv_screen_title)).setText("Search Users");
        // click listener to the menu icon to open the popup menu
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(SearchUsersActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(SearchUsersActivity.this, EditProfileActivity.class));
                    return true;
                    //} else if (id == R.id.menu_settings) {
                    //startActivity(new Intent(AddMoodActivity.this, SettingsActivity.class));
                    //return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(SearchUsersActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });;

        // --- Setup Bottom Navigation ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> startActivity(new Intent(SearchUsersActivity.this, HomeActivity.class)));
        btnSearch.setOnClickListener(v -> startActivity(new Intent(SearchUsersActivity.this, MapHandlerActivity.class)));
        btnAdd.setOnClickListener(v -> startActivity(new Intent(SearchUsersActivity.this, AddMoodActivity.class)));
        btnCalendar.setOnClickListener(v -> startActivity(new Intent(SearchUsersActivity.this, MoodHistoryActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(SearchUsersActivity.this, EditProfileActivity.class)));

        // --- Setup Other UI Elements ---
        Button btnExploreMap = findViewById(R.id.exploreMap);
        Button btnTabFriends = findViewById(R.id.tabFriends);
        Button btnTabRequests = findViewById(R.id.tabRequests);

        db = FirebaseFirestore.getInstance();
        editSearch = findViewById(R.id.editSearch);
        recyclerUsers = findViewById(R.id.recyclerUsers);

        // ✅ Get current username from Intent
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Setup RecyclerView and adapter
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(currentUsername);
        recyclerUsers.setAdapter(adapter);

        // ✅ Button: Explore Map
        btnExploreMap.setOnClickListener(v -> {
            Intent intent = new Intent(SearchUsersActivity.this, MapHandlerActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // ✅ Button: Friend Requests
        btnTabRequests.setOnClickListener(v -> {
            Intent intent = new Intent(SearchUsersActivity.this, FriendRequestActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // ✅ Button: Friend List
        btnTabFriends.setOnClickListener(v -> {
            Intent intent = new Intent(SearchUsersActivity.this, FriendListActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // ✅ Search Users (by username)
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            adapter.setUsers(new ArrayList<>());
            return;
        }

        db.collection("Users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SearchUser> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SearchUser user = document.toObject(SearchUser.class);
                        users.add(user);
                    }
                    adapter.setUsers(users);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {
    private EditText editSearch;
    private RecyclerView recyclerUsers;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_for_participant_result_screen);


        Button btnSearch = findViewById(R.id.exploreMap);


        db = FirebaseFirestore.getInstance();
        editSearch = findViewById(R.id.editSearch);
        recyclerUsers = findViewById(R.id.recyclerUsers);

        // Setup RecyclerView
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        recyclerUsers.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(SearchUsersActivity.this, MapHandlerActivity.class);
            startActivity(intent);
        });

        // Search functionality
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

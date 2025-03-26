package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private TextView textWelcome, textMoodCount;
    private Button btnQuickAddMood;
    private ListView listRecentMoods;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI components
        textWelcome = findViewById(R.id.textWelcome);
        textMoodCount = findViewById(R.id.textMoodCount);
        btnQuickAddMood = findViewById(R.id.btnQuickAddMood);
        listRecentMoods = findViewById(R.id.mood_entries_scroll);

        // Show user's display name in welcome message
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String displayName = querySnapshot.getDocuments().get(0).getString("displayName");
                            textWelcome.setText("Welcome, " + displayName + "!");
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show());
        }

        // ViewModel setup
        moodEventViewModel = MoodEventViewModel.getInstance();

        // Fetch moods and show list
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            masterMoodList.clear();
            masterMoodList.addAll(moodList);

            Collections.sort(masterMoodList, (m1, m2) ->
                    m2.getCreatedAt().toDate().compareTo(m1.getCreatedAt().toDate())
            );

            if (adapter == null) {
                adapter = new MoodListAdapter(HomeActivity.this, masterMoodList);
                listRecentMoods.setAdapter(adapter);
            } else {
                adapter.updateList(masterMoodList);
            }

            textMoodCount.setText("You’ve logged " + masterMoodList.size() + " moods.");
        });

        // Launch AddMoodActivity from "Quick Add Mood" button
        btnQuickAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });

        // Launch details view on item click
        listRecentMoods.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, MoodDetailsActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        });
    }
}

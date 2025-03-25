package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private TextView textWelcome, textMoodCount;
    private ImageButton btnQuickAddMood; // Using the same button id as in MoodHistoryActivity ("btn_add")
    private ListView listRecentMoods;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);  // Ensure that activity_home.xml defines a ListView with id "mood_entries_scroll"

        // Initialize UI components
        textWelcome = findViewById(R.id.textWelcome);
        textMoodCount = findViewById(R.id.textMoodCount);
        btnQuickAddMood = findViewById(R.id.btn_add);
        listRecentMoods = findViewById(R.id.mood_entries_scroll);

        // Set a welcome message
        textWelcome.setText("Welcome, Bashir!");

        // Use the same ViewModel instance as MoodHistoryActivity to ensure consistency
        moodEventViewModel = MoodEventViewModel.getInstance();

        // Fetch mood events and update the list view
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            masterMoodList.clear();
            masterMoodList.addAll(moodList);

            // Sort mood events in reverse chronological order (most recent first)
            Collections.sort(masterMoodList, (m1, m2) ->
                    m2.getCreatedAt().toDate().compareTo(m1.getCreatedAt().toDate())
            );

            // Set up the adapter if not already created; else update the list
            if (adapter == null) {
                adapter = new MoodListAdapter(HomeActivity.this, masterMoodList);
                listRecentMoods.setAdapter(adapter);
            } else {
                adapter.updateList(masterMoodList);
            }

            // Update the mood count text
            textMoodCount.setText("You’ve logged " + masterMoodList.size() + " moods.");
        });

        // Quick Add Mood button launches the AddMoodActivity
        btnQuickAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });

        // Handle list item clicks to launch MoodDetailsActivity with the selected mood's position
        listRecentMoods.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, MoodDetailsActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        });
    }
}

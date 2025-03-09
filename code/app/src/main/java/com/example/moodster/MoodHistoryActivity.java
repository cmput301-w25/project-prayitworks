package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoodHistoryActivity extends AppCompatActivity {
    private ListView listView;
    private MoodListAdapter adapter;
    private List<MoodEvent> moodEventList;
    private MoodEventViewModel moodEventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history_main);

        ImageButton btnAddMood = findViewById(R.id.btn_add);
        listView = findViewById(R.id.mood_entries_scroll);
        moodEventViewModel = MoodEventViewModel.getInstance();

        // Retrieve Mood Events from HashMap
        Map<Integer, MoodEvent> moodEventsMap = moodEventViewModel.getMoodEvents();
        moodEventList = new ArrayList<>(moodEventsMap.values());
        if (moodEventList.isEmpty()) {
            System.out.println("No mood events found");
        }

        // Set up adapter
        adapter = new MoodListAdapter(this, moodEventList);
        listView.setAdapter(adapter);

        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });
    }
}
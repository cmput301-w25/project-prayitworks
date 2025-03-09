package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoodHistoryActivity extends AppCompatActivity {
    private ListView listView;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList;
    private MoodEventViewModel moodEventViewModel;

    // Search field and empty state view for filtering
    private EditText searchReasonEditText;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history_main);

        ImageButton btnAddMood = findViewById(R.id.btn_add);
        listView = findViewById(R.id.mood_entries_scroll);
        searchReasonEditText = findViewById(R.id.editSearchReason);
        emptyStateTextView = findViewById(R.id.textEmptyState);
        emptyStateTextView.setVisibility(TextView.GONE);
        moodEventViewModel = MoodEventViewModel.getInstance();

        // Retrieve Mood Events from HashMap
        Map<Integer, MoodEvent> moodEventsMap = moodEventViewModel.getMoodEvents();
        masterMoodList = new ArrayList<>(moodEventsMap.values());
        if (masterMoodList.isEmpty()) {
            System.out.println("No mood events found");
        }

        // Set up adapter
        adapter = new MoodListAdapter(this, masterMoodList);
        listView.setAdapter(adapter);

        // Listen for text changes in the search field to filter the list dynamically
        searchReasonEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMoodList(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });
    }

    private void filterMoodList(String keyword) {
        // Delegate filtering to the separate filter class
        List<MoodEvent> filteredList = MoodEventFilter.filterByExplanation(masterMoodList, keyword);
        if (filteredList.isEmpty()) {
            emptyStateTextView.setVisibility(TextView.VISIBLE);
            emptyStateTextView.setText("No results found");
        } else {
            emptyStateTextView.setVisibility(TextView.GONE);
        }
        adapter.updateList(filteredList);
    }
}
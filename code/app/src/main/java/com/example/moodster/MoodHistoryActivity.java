package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList = new ArrayList<>();

    private Spinner spinnerFilterType;
    private EditText searchEditText;
    private TextView emptyStateTextView;
    private MoodEventViewModel moodEventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history_main);

        listView = findViewById(R.id.mood_entries_scroll);
        emptyStateTextView = findViewById(R.id.textEmptyState);
        emptyStateTextView.setVisibility(TextView.GONE);
        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        searchEditText = findViewById(R.id.editSearch);

        moodEventViewModel = MoodEventViewModel.getInstance();

        // 1) Fetch from Firestore if online, else local
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            masterMoodList.clear();
            masterMoodList.addAll(moodList);

            // Sort the mood events in reverse chronological order (most recent first)
            Collections.sort(masterMoodList, (m1, m2) -> m2.getCreatedAt().toDate().compareTo(m1.getCreatedAt().toDate()));

            if (adapter == null) {
                adapter = new MoodListAdapter(this, masterMoodList);
                listView.setAdapter(adapter);
            } else {
                adapter.updateList(masterMoodList);
            }
        });

        // Spinner change => update hint & apply filter
        spinnerFilterType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                updateSearchHint(selected);
                filterMoodList(searchEditText.getText().toString(), selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Typing triggers filtering
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                String selectedFilter = spinnerFilterType.getSelectedItem().toString();
                filterMoodList(query, selectedFilter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });


        // "+" button => AddMoodActivity
        ImageButton btnAddMood = findViewById(R.id.btn_add);
        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });
    }

    // Main filtering logic
    private void filterMoodList(String keyword, String filterType) {
        List<MoodEvent> filtered = new ArrayList<>();

        for (MoodEvent event : masterMoodList) {
            String fieldToMatch = "";

            switch (filterType) {
                case "Reason":
                    fieldToMatch = event.getExplanation();
                    break;
                case "Emotional State":
                    fieldToMatch = event.getEmotionalState();
                    break;
                case "Social Situation":
                    fieldToMatch = event.getSocialSituation();
                    break;
            }

            if (fieldToMatch != null && fieldToMatch.toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(event);
            }
        }

        if (filtered.isEmpty()) {
            emptyStateTextView.setVisibility(TextView.VISIBLE);
            emptyStateTextView.setText("No results found");
        } else {
            emptyStateTextView.setVisibility(TextView.GONE);
        }

        adapter.updateList(filtered);
    }

    // Dynamically update EditText hint
    private void updateSearchHint(String filterType) {
        switch (filterType) {
            case "Reason":
                searchEditText.setHint("Search by reason...");
                break;
            case "Emotional State":
                searchEditText.setHint("Search by emotional state...");
                break;
            case "Social Situation":
                searchEditText.setHint("Search by social situation...");
                break;
        }
    }
}
package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList = new ArrayList<>();

    private EditText searchReasonEditText;
    private TextView emptyStateTextView;
    private MoodEventViewModel moodEventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history_main);

        listView = findViewById(R.id.mood_entries_scroll);
        searchReasonEditText = findViewById(R.id.editSearchReason);
        emptyStateTextView = findViewById(R.id.textEmptyState);
        emptyStateTextView.setVisibility(TextView.GONE);

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

        // 2) Filter by explanation
        searchReasonEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterMoodList(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // "+" button => AddMoodActivity
        ImageButton btnAddMood = findViewById(R.id.btn_add);
        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });
    }

    private void filterMoodList(String keyword) {
        List<MoodEvent> filtered = MoodEventFilter.filterByExplanation(masterMoodList, keyword);

        if (filtered.isEmpty()) {
            emptyStateTextView.setVisibility(TextView.VISIBLE);
            emptyStateTextView.setText("No results found");
        } else {
            emptyStateTextView.setVisibility(TextView.GONE);
        }
        adapter.updateList(filtered);
    }
}
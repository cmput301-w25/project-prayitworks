package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MoodEventViewModel moodEventViewModel;
    private Spinner spinnerEmotionalState, spinnerFilter;
    private EditText editTrigger, editSocialSituation, editExplanation;
    private Button btnAddMood;
    private String selectedEmotionalState = "";
    private TextView explanationCharCount;

    private ListView moodListView;
    private ArrayAdapter<String> moodListAdapter;
    private List<MoodEvent> moodEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Setup UI
        moodListView = findViewById(R.id.moodListView);
        moodListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        moodListView.setAdapter(moodListAdapter);

        moodEventViewModel = new ViewModelProvider(this).get(MoodEventViewModel.class);

        spinnerEmotionalState = findViewById(R.id.spinnerEmotionalState);
        spinnerFilter = findViewById(R.id.spinnerFilterEmotionalState);
        editTrigger = findViewById(R.id.editTrigger);
        editSocialSituation = findViewById(R.id.editSocialSituation);
        btnAddMood = findViewById(R.id.btnAddMood);
        editExplanation = findViewById(R.id.editExplanation);
        explanationCharCount = findViewById(R.id.textExplanationCount);

        // Setup emotional-state spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                MoodEvent.VALID_EMOTIONAL_STATES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmotionalState.setAdapter(adapter);

        spinnerEmotionalState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEmotionalState = MoodEvent.VALID_EMOTIONAL_STATES.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEmotionalState = "";
            }
        });

        // Limit explanation to 20 chars
        editExplanation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        // Show character count
        editExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                explanationCharCount.setText(s.length() + "/20");
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Setup filter spinner
        List<String> filterOptions = new ArrayList<>(MoodEvent.VALID_EMOTIONAL_STATES);
        filterOptions.add(0, "All Moods");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, filterOptions
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = filterOptions.get(position);
                filterMoodList(selectedFilter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Add mood button
        btnAddMood.setOnClickListener(v -> {
            if (selectedEmotionalState.isEmpty()) {
                Log.d("MoodEvent", "Error: No emotional state selected.");
                return;
            }

            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = editSocialSituation.getText().toString().trim();
            String explanation = editExplanation.getText().toString().trim();

            // We create an ID from moodEventList.size(), or you can pass 0 to let the ViewModel generate one
            int id = moodEventList.size();

            Timestamp timestamp = Timestamp.now();
            MoodEvent newMood = new MoodEvent(
                    id,
                    timestamp,
                    selectedEmotionalState,
                    trigger,
                    socialSituation,
                    explanation,
                    null,
                    0,
                    0
            );
            moodEventViewModel.addMoodEvent(newMood);
            moodEventList.add(newMood);

            moodListAdapter.add(selectedEmotionalState + " - " + timestamp);
            Log.d("MoodEvent", "All Moods: " + moodEventList);
        });

        // On item click => show details
        if (moodListView != null) {
            moodListView.setOnItemClickListener((parent, view, position, id) -> {
                MoodEvent selectedMood = moodEventList.get(position);
                Intent intent = new Intent(MainActivity.this, MoodDetailsActivity.class);
                intent.putExtra("mood", selectedMood);
                startActivity(intent);
                Log.d("MoodEvent", "Working!");
            });
        } else {
            Log.e("MainActivity", "ListView is null!");
        }
    }

    // Filter by emotional state in memory
    private void filterMoodList(String filter) {
        moodListAdapter.clear();
        for (MoodEvent mood : moodEventList) {
            if (filter.equals("All Moods") || mood.getEmotionalState().equals(filter)) {
                moodListAdapter.add(mood.getEmotionalState() + " - " + new Date(mood.getCreatedAt().toDate().getTime()));
            }
        }
    }
}

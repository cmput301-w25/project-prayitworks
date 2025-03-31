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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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
    private final List<MoodEvent> moodEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Setup UI for add mood form (inside form_container)
        spinnerEmotionalState = findViewById(R.id.spinnerEmotionalState);
        spinnerFilter = findViewById(R.id.spinnerFilterEmotionalState);
        editTrigger = findViewById(R.id.editTrigger);
        editSocialSituation = findViewById(R.id.editSocialSituation);
        btnAddMood = findViewById(R.id.btnAddMood);
        editExplanation = findViewById(R.id.editExplanation);
        explanationCharCount = findViewById(R.id.textExplanationCount);
        moodListView = findViewById(R.id.moodListView);

        // Setup spinners and adapter for emotional states
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

        // Limit explanation to 20 chars and show character count
        editExplanation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
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

        // Setup mood list and adapter
        moodListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        moodListView.setAdapter(moodListAdapter);

        moodEventViewModel = new ViewModelProvider(this).get(MoodEventViewModel.class);

        // Add mood button listener
        btnAddMood.setOnClickListener(v -> {
            if (selectedEmotionalState.isEmpty()) {
                Log.d("MoodEvent", "Error: No emotional state selected.");
                return;
            }

            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = editSocialSituation.getText().toString().trim();
            String explanation = editExplanation.getText().toString().trim();

            moodEventViewModel.addMoodEvent(
                    selectedEmotionalState,
                    trigger,
                    socialSituation,
                    explanation,
                    null, // image
                    0, 0,
                    new MoodEventViewModel.OnAddListener() {
                        @Override
                        public void onAddSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Mood added!", Toast.LENGTH_SHORT).show();
                                // Reload list from ViewModel if needed
                                moodEventViewModel.fetchCurrentUserMoods(moods -> {
                                    moodEventList.clear();
                                    moodEventList.addAll(moods);
                                    filterMoodList(spinnerFilter.getSelectedItem().toString());
                                });
                            });
                        }

                        @Override
                        public void onAddFailure(String errorMessage) {
                            Log.e("MoodEvent", "Failed to add mood: " + errorMessage);
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Failed to add mood.", Toast.LENGTH_SHORT).show());
                        }
                    }
            );
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

        // Home button click listener: go to HomeActivity (or HomeFragment if using NavController)
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            // In this example, we launch HomeActivity.
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    // Filter by emotional state in memory; declared outside onCreate
    private void filterMoodList(String filter) {
        moodListAdapter.clear();
        for (MoodEvent mood : moodEventList) {
            if (filter.equals("All Moods") || mood.getEmotionalState().equals(filter)) {
                moodListAdapter.add(mood.getEmotionalState() + " - " +
                        new Date(mood.getCreatedAt().toDate().getTime()));
            }
        }
    }
}

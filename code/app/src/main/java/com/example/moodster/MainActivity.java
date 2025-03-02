package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.moodster.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private MoodEventViewModel moodEventViewModel;
    private Spinner spinnerEmotionalState;
    private EditText editTrigger, editSocialSituation, editExplanation;
    private Button btnAddMood, btnViewMoods;
    private String selectedEmotionalState = ""; // Holds the selected state
    private TextView explanationCharCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        moodEventViewModel = MoodEventViewModel.getInstance();

        // Find views
        spinnerEmotionalState = findViewById(R.id.spinnerEmotionalState);
        editTrigger = findViewById(R.id.editTrigger);
        editSocialSituation = findViewById(R.id.editSocialSituation);
        btnAddMood = findViewById(R.id.btnAddMood);
        btnViewMoods = findViewById(R.id.btnViewMoods);
        editExplanation = findViewById(R.id.editExplanation);
        explanationCharCount = findViewById(R.id.textExplanationCount);

        // Set up Spinner (Dropdown)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, MoodEvent.VALID_EMOTIONAL_STATES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmotionalState.setAdapter(adapter);

        // Get selected item from Spinner
        spinnerEmotionalState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEmotionalState = MoodEvent.VALID_EMOTIONAL_STATES.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEmotionalState = ""; // Reset if nothing is selected
            }
        });

        // Filter input field to 20 characters
        editExplanation.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });

        // TextWatcher: Update character count display
        editExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                explanationCharCount.setText(s.length() + "/20"); // update TextView to char count
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Button: Add Mood Event
        btnAddMood.setOnClickListener(v -> {
            if (selectedEmotionalState.isEmpty()) {
                Log.d("MoodEvent", "Error: No emotional state selected.");
                return;
            }

            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = editSocialSituation.getText().toString().trim();
            String explanation = editExplanation.getText().toString().trim();

            moodEventViewModel.addMoodEvent(selectedEmotionalState, trigger, socialSituation, explanation);
            Log.d("MoodEvent", "New MoodEvent added: " + selectedEmotionalState);
        });

        // Button: View Stored Mood Events
        btnViewMoods.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListViewActivity.class);
            startActivity(intent);
        });
    }
}
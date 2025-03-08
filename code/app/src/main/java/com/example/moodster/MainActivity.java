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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.firebase.Timestamp;

public class MainActivity extends AppCompatActivity {
    private MoodEventViewModel moodEventViewModel;
    private Spinner spinnerEmotionalState, spinnerFilter;
    ;
    private EditText editTrigger, editSocialSituation, editExplanation;
    private Button btnAddMood, btnViewMoods;
    private String selectedEmotionalState = ""; // Holds the selected state
    private TextView explanationCharCount;


    private ListView moodListView;
    private ArrayAdapter<String> moodListAdapter;
    private List<MoodEvent> moodEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating the List for the moods
        moodListView = findViewById(R.id.moodListView);
        moodListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        moodListView.setAdapter(moodListAdapter);

        // Initialize ViewModel
        moodEventViewModel = new ViewModelProvider(this).get(MoodEventViewModel.class);

        // Find views
        spinnerEmotionalState = findViewById(R.id.spinnerEmotionalState);
        spinnerFilter = findViewById(R.id.spinnerFilterEmotionalState);
        editTrigger = findViewById(R.id.editTrigger);
        editSocialSituation = findViewById(R.id.editSocialSituation);
        btnAddMood = findViewById(R.id.btnAddMood);
        //btnViewMoods = findViewById(R.id.btnViewMoods);
        editExplanation = findViewById(R.id.editExplanation);
        explanationCharCount = findViewById(R.id.textExplanationCount);

        /// SELECTING A MOOD!!!! START
        // Explanation: Creating Spinner based on the valid emotional state, Setting the mood according to the selection
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
                //Log.d("Position", "Number: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEmotionalState = ""; // Reset if nothing is selected
            }
        });
        /// SELECTING A MOOD!!!! END

        // Filter input field to 20 characters
        editExplanation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        // TextWatcher: Update character count display
        editExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                explanationCharCount.setText(s.length() + "/20"); // update TextView to char count
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        /// FILTERING BASED ON SELECTED MOOD!!!! START
        // Explanation: Creating Spinner based on the valid emotional state, getting the item selected and passing it
        // to the Filter Mood function

        // Set up filter spinner
        List<String> filterOptions = new ArrayList<>(MoodEvent.VALID_EMOTIONAL_STATES);
        filterOptions.add(0, "All Moods"); // Add an option to show all moods

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, filterOptions
        );

        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        // Filter mood list based on selected filter
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = filterOptions.get(position);
                filterMoodList(selectedFilter);
                //Log.d("Position", "Number: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /// FILTERING BASED ON SELECTED MOOD!!!! END

        // Filter input field to 20 characters
        editExplanation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});


        /// ADDING EMOTIONS/ MAKING EMOTIONS!! START
        // Button: Add Mood Event
        btnAddMood.setOnClickListener(v -> {
            if (selectedEmotionalState.isEmpty()) {
                Log.d("MoodEvent", "Error: No emotional state selected.");
                return;
            }

            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = editSocialSituation.getText().toString().trim();
            String explanation = editExplanation.getText().toString().trim();
            int id = moodEventList.size();
            Timestamp timestamp = Timestamp.now();

            MoodEvent newMood = new MoodEvent(id, timestamp, selectedEmotionalState , trigger, socialSituation, explanation);
            moodEventViewModel.addMoodEvent(newMood); // Adding to the Hashmap
            moodEventList.add(newMood);
            moodListAdapter.add(selectedEmotionalState + " - " + timestamp);
            Log.d("MoodEvent", "All Moods: " + moodEventList);
        });
        /// ADDING EMOTIONS/ MAKING EMOTIONS!! END


        /// LIST OF ADDED EMOTIONS!! START
        // ListView Item Click: View Mood Details

        if (moodListView != null) {
            moodListView.setOnItemClickListener((parent, view, position, id) -> {
                MoodEvent selectedMood = moodEventList.get(position);
                Intent intent = new Intent(MainActivity.this, MoodDetailsActivity.class);
                intent.putExtra("mood", selectedMood);
                startActivity(intent);
                Log.d("MoodEvent", "Working!");
            });
        } else {
            Log.e("MainActivity", "ListView is null! Check XML or findViewById()");
        }
        /// LIST OF ADDED EMOTIONS!! END

    }
    // Logic: Clear the Mood List Adapter (Used to create the list) then add the moods depending on the chosen mood
    private void filterMoodList(String filter) {
        moodListAdapter.clear();
        for (MoodEvent mood : moodEventList) {
            if (filter.equals("All Moods") || mood.getEmotionalState().equals(filter)) {
                moodListAdapter.add(mood.getEmotionalState() + " - " + new Date(String.valueOf(mood.getCreatedAt())));
            }
        }
    }
}
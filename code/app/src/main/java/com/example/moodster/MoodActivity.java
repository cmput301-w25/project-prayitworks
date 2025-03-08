package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class MoodActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private Spinner spinnerEmotionalState;
    private EditText editTrigger, editSocialSituation, editExplanation;
    private Button btnSave, btnCancel;
    private ImageButton btnViewMoodHistory;
    private String selectedEmotionalState; // Holds the selected state
    private TextView explanationCharCount;
    private ArrayAdapter<String> moodListAdapter;
    private List<MoodEvent> moodEventList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedEmotionalState = getIntent().getStringExtra("mood");

        if ("angry".equals(selectedEmotionalState)) {
            setContentView(R.layout.angry_mood);
        } else if ("sad".equals(selectedEmotionalState)) {
            setContentView(R.layout.sad_mood);
        } else if ("shame".equals(selectedEmotionalState)) {
            setContentView(R.layout.shame_mood);
        } else if ("fear".equals(selectedEmotionalState)) {
            setContentView(R.layout.fear_mood);
        } else if ("happy".equals(selectedEmotionalState)) {
            setContentView(R.layout.happy_mood);
        }

        // Initialize ViewModel
        moodEventViewModel = new ViewModelProvider(this).get(MoodEventViewModel.class);

        // Find views
        editExplanation = findViewById(R.id.edit_reason);
        //explanationCharCount = findViewById(R.id.textExplanationCount);
        editTrigger = findViewById(R.id.edit_trigger);
        editSocialSituation = findViewById(R.id.editSocialSituation);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        btnViewMoodHistory = findViewById(R.id.btn_calendar);



        // Filter input field to 20 characters
        editExplanation.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        // TextWatcher: Update character count display
        /*
        editExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                explanationCharCount.setText(s.length() + "/20"); // update TextView to char count
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });*/


        btnSave.setOnClickListener(v -> {
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = "editSocialSituation.getText().toString().trim()";
            String explanation = editExplanation.getText().toString().trim();

            int id = 0;
            Timestamp timestamp = Timestamp.now();

            MoodEvent newMood = new MoodEvent(id, timestamp, selectedEmotionalState , trigger, socialSituation, explanation);
            moodEventViewModel.addMoodEvent(newMood); // Adding to the Hashmap

            Log.d("MoodEvent", "All Moods: " + moodEventList);
        });

        btnCancel.setOnClickListener(v -> {
            finish();
        });

    }
}
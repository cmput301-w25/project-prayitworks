package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class EditMoodActivity extends AppCompatActivity {

    public static final List<String> VALID_EMOTIONAL_STATES = Arrays.asList(
            "Anger", "Confusion", "Disgust", "Fear", "Happiness", "Sadness", "Shame", "Surprise"
    );

    public static final List<String> VALID_SOCIAL_SITUATION = Arrays.asList(
            "Alone, with one other person",
            "With two to several people",
            "With a crowd"
    );

    private EditText reasonValue, triggerValue;
    private Spinner spinnerMood, spinnerSocialSituation;
    private Switch switchVisibility;
    private MoodEvent eventToEdit;
    private String moodId;
    private String currentUsername;
    private MoodEventViewModel moodEventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);

        moodEventViewModel = MoodEventViewModel.getInstance();

        // ✅ Grab and validate username
        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        moodId = intent.getStringExtra("moodId");

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        moodEventViewModel.setUsername(currentUsername);

        reasonValue = findViewById(R.id.textReasonValue);
        triggerValue = findViewById(R.id.textTriggerValue);
        spinnerMood = findViewById(R.id.textMoodEmoji);
        spinnerSocialSituation = findViewById(R.id.textSocialValue);
        switchVisibility = findViewById(R.id.textVisibilityValue);

        // limiting reason and trigger fields to 20 characters
        reasonValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        triggerValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, VALID_EMOTIONAL_STATES
        );
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(moodAdapter);

        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATION
        );
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocialSituation.setAdapter(socialAdapter);

        eventToEdit = moodEventViewModel.getMoodEvents().get(moodId);

        if (eventToEdit != null) {
            reasonValue.setText(eventToEdit.getExplanation());
            triggerValue.setText(eventToEdit.getTrigger());
            setSpinnerSelection(spinnerMood, eventToEdit.getEmotionalState());
            setSpinnerSelection(spinnerSocialSituation, eventToEdit.getSocialSituation());
            switchVisibility.setChecked(eventToEdit.isPublic());
        }

        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> finish());

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> onSaveClicked());
    }

    private void onSaveClicked() {
        if (eventToEdit != null) {
            eventToEdit.setExplanation(reasonValue.getText().toString());
            eventToEdit.setTrigger(triggerValue.getText().toString());
            eventToEdit.setEmotionalState(spinnerMood.getSelectedItem().toString());
            eventToEdit.setSocialSituation(spinnerSocialSituation.getSelectedItem().toString());
            eventToEdit.setPublic(switchVisibility.isChecked());

            moodEventViewModel.updateMoodEvent(eventToEdit, new MoodEventViewModel.OnUpdateListener() {
                @Override
                public void onUpdateSuccess() {
                    Intent backToHistory = new Intent(EditMoodActivity.this, MoodHistoryActivity.class);
                    backToHistory.putExtra("username", currentUsername); // ✅
                    backToHistory.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(backToHistory);
                    finish();
                }

                @Override
                public void onUpdateFailure(String errorMessage) {
                    Toast.makeText(EditMoodActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            String item = spinner.getAdapter().getItem(i).toString();
            if (item.equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}

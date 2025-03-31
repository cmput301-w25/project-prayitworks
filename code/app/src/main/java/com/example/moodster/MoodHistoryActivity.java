package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

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

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_history_main);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.setUsername(currentUsername);

        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("Mood History");
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MoodHistoryActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    Intent intent = new Intent(MoodHistoryActivity.this, EditProfileActivity.class);
                    intent.putExtra("username", currentUsername);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MoodHistoryActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        listView = findViewById(R.id.mood_entries_scroll);
        emptyStateTextView = findViewById(R.id.textEmptyState);
        emptyStateTextView.setVisibility(TextView.GONE);
        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        searchEditText = findViewById(R.id.editSearch);

        // Fetch moods
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            masterMoodList.clear();
            masterMoodList.addAll(moodList);

            Collections.sort(masterMoodList, (m1, m2) ->
                    m2.getCreatedAt().toDate().compareTo(m1.getCreatedAt().toDate()));

            if (adapter == null) {
                adapter = new MoodListAdapter(this, masterMoodList, currentUsername);
                listView.setAdapter(adapter);
            } else {
                adapter.updateList(masterMoodList);
            }
        });

        // Spinner filter change
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

        // Search bar logic
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMoodList(s.toString(), spinnerFilterType.getSelectedItem().toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // "+" Add Mood
        ImageButton btnAddMood = findViewById(R.id.btn_add);
        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMoodActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        // Click on mood => view details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selected = masterMoodList.get(position);
            Intent intent = new Intent(MoodHistoryActivity.this, MoodDetailsActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("moodId", selected.getMoodId());
            intent.putExtra("textMoodEmoji", selected.getEmoji());
            intent.putExtra("textMoodDateTime", selected.getCreatedAt().toDate().toString());
            intent.putExtra("textReasonValue", selected.getExplanation());
            intent.putExtra("textTriggerValue", selected.getTrigger());
            intent.putExtra("textSocialValue", selected.getSocialSituation());
            intent.putExtra("imageMoodPhoto", selected.getImage());
            startActivity(intent);
        });

        // --- Bottom Nav Buttons ---
        findViewById(R.id.btn_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_search).setOnClickListener(v ->
                startActivity(new Intent(this, MapHandlerActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_calendar).setOnClickListener(v ->
                startActivity(new Intent(this, MoodHistoryActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddMoodActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class).putExtra("username", currentUsername)));
    }

    private void filterMoodList(String keyword, String filterType) {
        if (adapter == null) return;
        List<MoodEvent> filtered = new ArrayList<>();
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000;

        for (MoodEvent event : masterMoodList) {
            boolean matches = false;

            if (filterType.equals("Most Recent Week")) {
                matches = event.getCreatedAt().toDate().getTime() >= sevenDaysAgo;
            } else {
                String fieldToMatch = "";
                switch (filterType) {
                    case "Reason": fieldToMatch = event.getExplanation(); break;
                    case "Emotional State": fieldToMatch = event.getEmotionalState(); break;
                    case "Social Situation": fieldToMatch = event.getSocialSituation(); break;
                }
                matches = fieldToMatch != null && fieldToMatch.toLowerCase().contains(keyword.toLowerCase());
            }

            if (matches) filtered.add(event);
        }

        emptyStateTextView.setVisibility(filtered.isEmpty() ? TextView.VISIBLE : TextView.GONE);
        emptyStateTextView.setText("No results found");
        adapter.updateList(filtered);
    }

    private void updateSearchHint(String filterType) {
        String hint = "";
        switch (filterType) {
            case "Reason": hint = "Search by reason..."; break;
            case "Emotional State": hint = "Search by emotional state..."; break;
            case "Social Situation": hint = "Search by social situation..."; break;
            case "Most Recent Week": hint = "Search by most recent week..."; break;
        }
        searchEditText.setHint(hint);
    }
}

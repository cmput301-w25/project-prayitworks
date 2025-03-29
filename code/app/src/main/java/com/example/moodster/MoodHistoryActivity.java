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
import com.google.firebase.firestore.FirebaseFirestore;

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
                    startActivity(new Intent(MoodHistoryActivity.this, EditProfileActivity.class));
                    return true;
                //} else if (id == R.id.menu_settings) {
                    //startActivity(new Intent(MoodHistoryActivity.this, SettingsActivity.class));
                    //return true;
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

        // --- Bottom Navigation Buttons ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, MapHandlerActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });

        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    // Main filtering logic
    private void filterMoodList(String keyword, String filterType) {
        if (adapter == null) return;
        List<MoodEvent> filtered = new ArrayList<>();

        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000;

        for (MoodEvent event : masterMoodList) {
            boolean matches = false;

            if (filterType.equals("Most Recent Week")) {
                long eventTime = event.getCreatedAt().toDate().getTime();
                matches = (eventTime >= sevenDaysAgo);
            } else {
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
                    matches = true;
                }
            }

            if (matches) {
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
            case "Most Recent Week":
                searchEditText.setHint("Search by most recent week...");
                break;
        }
    }
}

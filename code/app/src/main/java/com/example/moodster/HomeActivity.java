package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private TextView textWelcome, textMoodCount;
    private Button btnQuickAddMood, btnManageFriends;
    private ListView listRecentMoods;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList = new ArrayList<>();

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get current username from intent
        currentUsername = getIntent().getStringExtra("username");

        // --- Custom Header Setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("Home");

        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        textWelcome = findViewById(R.id.textWelcome);
        textMoodCount = findViewById(R.id.textMoodCount);
        btnQuickAddMood = findViewById(R.id.btnQuickAddMood);
        listRecentMoods = findViewById(R.id.mood_entries_scroll);
        btnManageFriends = findViewById(R.id.btnManageFriends);

        // Welcome display
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String displayName = querySnapshot.getDocuments().get(0).getString("displayName");
                            textWelcome.setText("Welcome, " + displayName + "!");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show());
        }

        // Fetch mood events
        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            masterMoodList.clear();
            masterMoodList.addAll(moodList);
            Collections.sort(masterMoodList, (m1, m2) ->
                    m2.getCreatedAt().toDate().compareTo(m1.getCreatedAt().toDate()));

            if (adapter == null) {
                adapter = new MoodListAdapter(HomeActivity.this, masterMoodList, currentUsername);

                listRecentMoods.setAdapter(adapter);
            } else {
                adapter.updateList(masterMoodList);
            }

            textMoodCount.setText("You’ve logged " + masterMoodList.size() + " moods.");
        });

        btnQuickAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });

        // ✅ Forward username to MoodDetailsActivity
        listRecentMoods.setOnItemClickListener((parent, view, position, id) -> {
            MoodEvent selected = masterMoodList.get(position);

            Intent intent = new Intent(HomeActivity.this, MoodDetailsActivity.class);
            intent.putExtra("username", currentUsername);
            intent.putExtra("moodId", selected.getMoodId());
            intent.putExtra("textMoodEmoji", selected.getEmoji());
            intent.putExtra("textMoodDateTime", selected.getCreatedAt().toDate().toString());
            intent.putExtra("textReasonValue", selected.getExplanation());
            intent.putExtra("textTriggerValue", selected.getTrigger());
            intent.putExtra("textSocialValue", selected.getSocialSituation());
            intent.putExtra("imageMoodPhoto", selected.getImage()); // Optional

            startActivity(intent);
        });

        btnManageFriends.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchUsersActivity.class);
            startActivity(intent);
        });

        // --- Bottom Navigation ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnViewMoodHistory = findViewById(R.id.btn_calendar);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AddMoodActivity.class)));
        btnSearch.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MapHandlerActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EditProfileActivity.class)));
        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodHistoryActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });
    }
}

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Update the screen title
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("Home");

        // Attach a click listener to the menu icon to open the popup menu
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
                    return true;
                //} else if (id == R.id.menu_settings) {
                //    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                //    return true;
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

        // --- Initialize the rest of UI components ---
        textWelcome = findViewById(R.id.textWelcome);
        textMoodCount = findViewById(R.id.textMoodCount);
        btnQuickAddMood = findViewById(R.id.btnQuickAddMood);
        listRecentMoods = findViewById(R.id.mood_entries_scroll);
        btnManageFriends = findViewById(R.id.btnManageFriends);

        // Show user's display name in welcome message
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

        // ViewModel setup and fetch moods
        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.fetchCurrentUserMoods(moodList -> {
            masterMoodList.clear();
            masterMoodList.addAll(moodList);

            Collections.sort(masterMoodList, (m1, m2) ->
                    m2.getCreatedAt().toDate().compareTo(m1.getCreatedAt().toDate())
            );

            if (adapter == null) {
                adapter = new MoodListAdapter(HomeActivity.this, masterMoodList);
                listRecentMoods.setAdapter(adapter);
            } else {
                adapter.updateList(masterMoodList);
            }

            textMoodCount.setText("You’ve logged " + masterMoodList.size() + " moods.");
        });

        // Quick Add Mood button: Launch AddMoodActivity
        btnQuickAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });

        // Launch details view on item click
        listRecentMoods.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HomeActivity.this, MoodDetailsActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        });

        // Quick Manage Friends Button: Launch SearchUser
        btnManageFriends.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchUsersActivity.class);
            startActivity(intent);
        });

        // --- Bottom Navigation Buttons ---
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnViewMoodHistory = findViewById(R.id.btn_calendar);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        // Bottom Navigation: Add Mood Screen
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation: Search / Map
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MapHandlerActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation: Profile (launch EditProfileActivity)
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation: Mood History (Calendar)
        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });
    }
}

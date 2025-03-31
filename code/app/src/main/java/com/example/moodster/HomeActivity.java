package com.example.moodster;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
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

/**
 * The HomeActivity class displays a welcome message, mood count, and a list of recent mood entries.
 * It handles navigation through the app using both a custom header with a popup menu.</p>
 */
public class HomeActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private TextView textWelcome, textMoodCount;
    private Button btnQuickAddMood, btnManageFriends;
    private ListView listRecentMoods;
    private MoodListAdapter adapter;
    private List<MoodEvent> masterMoodList = new ArrayList<>();
    private Handler scrollHandler = new Handler();
    private Runnable scrollRunnable;
    private int scrollIndex = 0;

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.setUsername(currentUsername);

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
                    Intent profileIntent = new Intent(HomeActivity.this, EditProfileActivity.class);
                    profileIntent.putExtra("username", currentUsername);
                    startActivity(profileIntent);
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

            scrollRunnable = new Runnable() {
                @Override
                public void run() {
                    if (adapter != null && adapter.getCount() > 0) {
                        listRecentMoods.smoothScrollToPosition(scrollIndex);
                        scrollIndex = (scrollIndex + 1) % adapter.getCount();
                        scrollHandler.postDelayed(this, 1000); // Adjust time interval as needed
                    }
                }
            };
            scrollHandler.postDelayed(scrollRunnable, 1000); // Start the scrolling
        });

        btnQuickAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

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
            intent.putExtra("imageMoodPhoto", selected.getImage());
            startActivity(intent);
        });

        btnManageFriends.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchUsersActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnViewMoodHistory = findViewById(R.id.btn_calendar);
        ImageButton btnAdd = findViewById(R.id.btn_add);
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMoodActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MapHandlerActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MoodHistoryActivity.class);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
        });

        setupNavBarBounceWithShadow();
        setupNavBarHoverEffects();
    }

    /**
     * Called when the activity is destroyed.
     *
     * <p>This method removes any pending callbacks in the scroll handler to prevent memory leaks.</p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        scrollHandler.removeCallbacks(scrollRunnable);  // Clean up scrolling handler
    }

    /**
     * Sets up hover effects for bottom navigation buttons.
     *
     * <p>This method assigns touch listeners to the navigation buttons to apply a lift animation on press and reset the animation on release.</p>
     */
    private void setupNavBarHoverEffects() {
        int[] navButtonIds = {
                R.id.btn_home, R.id.btn_search, R.id.btn_add, R.id.btn_calendar, R.id.btn_profile
        };

        for (int id : navButtonIds) {
            View button = findViewById(id);

            button.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        AnimatorSet lift = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.nav_hover_lift);
                        lift.setTarget(v);
                        lift.start();
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        AnimatorSet reset = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.nav_hover_reset);
                        reset.setTarget(v);
                        reset.start();
                        return false;
                }
                return false;
            });
        }
    }

    /**
     * Sets up bounce animation with shadow effect for bottom navigation buttons.
     *
     * <p>This method assigns click listeners to the navigation buttons to apply a bounce animation.
     * It resets the scale and elevation of all buttons, then applies a bounce effect to the clicked button,
     * and then navigates to the corresponding activity.</p>
     */
    private void setupNavBarBounceWithShadow() {
        int[] navIds = {
                R.id.btn_home, R.id.btn_search, R.id.btn_add,
                R.id.btn_calendar, R.id.btn_profile
        };

        for (int id : navIds) {
            ImageButton button = findViewById(id);

            button.setOnClickListener(v -> {
                for (int resetId : navIds) {
                    ImageButton resetBtn = findViewById(resetId);
                    resetBtn.setElevation(4f);
                    resetBtn.setScaleX(1f);
                    resetBtn.setScaleY(1f);
                }

                AnimatorSet bounce = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.nav_bounce);
                bounce.setTarget(button);
                bounce.start();

                button.setElevation(12f);

                Intent intent = null;
                if (id == R.id.btn_home) {
                    intent = new Intent(this, HomeActivity.class);
                } else if (id == R.id.btn_search) {
                    intent = new Intent(this, MapHandlerActivity.class);
                } else if (id == R.id.btn_add) {
                    intent = new Intent(this, AddMoodActivity.class);
                } else if (id == R.id.btn_calendar) {
                    intent = new Intent(this, MoodHistoryActivity.class);
                } else if (id == R.id.btn_profile) {
                    intent = new Intent(this, EditProfileActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("username", currentUsername);
                    startActivity(intent);
                }
            });
        }
    }
}

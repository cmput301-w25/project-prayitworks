package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class AddMoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_mood);

        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set header title to "Add Mood"
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("Add Mood");

        // click listener to the menu icon to open the popup menu
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(AddMoodActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(AddMoodActivity.this, EditProfileActivity.class));
                    return true;
                //} else if (id == R.id.menu_settings) {
                    //startActivity(new Intent(AddMoodActivity.this, SettingsActivity.class));
                    //return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(AddMoodActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // ---  AddMoodActivity functionality ---
        Button btnAngry = findViewById(R.id.btn_angry);
        Button btnConfusion = findViewById(R.id.btn_confusion);
        Button btnDisgust = findViewById(R.id.btn_disgust);
        Button btnSad = findViewById(R.id.btn_sad);
        Button btnFear = findViewById(R.id.btn_fear);
        Button btnShame = findViewById(R.id.btn_shame);
        Button btnHappy = findViewById(R.id.btn_happy);
        Button btnSurprise = findViewById(R.id.btn_surprise);

        ImageButton btnViewMoodHistory = findViewById(R.id.btn_calendar);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnHome = findViewById(R.id.btn_home);

        btnAngry.setOnClickListener(view -> openMoodActivity("Anger"));
        btnConfusion.setOnClickListener(view -> openMoodActivity("Confusion"));
        btnDisgust.setOnClickListener(view -> openMoodActivity("Disgust"));
        btnSad.setOnClickListener(view -> openMoodActivity("Sadness"));
        btnFear.setOnClickListener(view -> openMoodActivity("Fear"));
        btnShame.setOnClickListener(view -> openMoodActivity("Shame"));
        btnHappy.setOnClickListener(view -> openMoodActivity("Happiness"));
        btnSurprise.setOnClickListener(view -> openMoodActivity("Surprise"));

        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(AddMoodActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(AddMoodActivity.this, SearchUsersActivity.class);
            startActivity(intent);
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(AddMoodActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void openMoodActivity(String mood) {
        Intent intent = new Intent(AddMoodActivity.this, MoodActivity.class);
        intent.putExtra("mood", mood);
        startActivity(intent);
    }
}

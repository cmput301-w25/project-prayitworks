package com.example.moodster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHandlerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng userLocation;
    private MoodEventViewModel moodEventViewModel;
    private Circle radiusCircle;
    private HashMap<String, String> emotionToEmoji = new HashMap<>();

    // UI Components
    private SeekBar seekBar;
    private Spinner spinnerFilterType;
    private EditText editSearch;
    private TextView radiusText;

    // Filtering state
    private String currentFilterType = "Emotional State";
    private String currentSearchKeyword = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    // ✅ Username tracking
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_radius_mood_map);

        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Username missing. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeEmojiMappings();
        initializeUIComponents();
        setupMap();
        setupFirestoreQuery();

        // ✅ Handle bottom nav
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.btn_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_search).setOnClickListener(v ->
                Toast.makeText(this, "Already on Map", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddMoodActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_calendar).setOnClickListener(v ->
                startActivity(new Intent(this, MoodHistoryActivity.class).putExtra("username", currentUsername)));
        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class).putExtra("username", currentUsername)));
        // --- Set up the custom header ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set header title
        TextView tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvScreenTitle.setText("Mood Map");

        // click listener to the menu icon to open the popup menu
        ImageView menuIcon = findViewById(R.id.ic_profile_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MapHandlerActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    startActivity(new Intent(MapHandlerActivity.this, EditProfileActivity.class));
                    return true;
                    //} else if (id == R.id.menu_settings) {
                    //startActivity(new Intent(AddMoodActivity.this, SettingsActivity.class));
                    //return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MapHandlerActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }


    private void initializeEmojiMappings() {
        emotionToEmoji.put("Anger", "😡");
        emotionToEmoji.put("Confusion", "😵‍💫");
        emotionToEmoji.put("Disgust", "🤢");
        emotionToEmoji.put("Fear", "😨");
        emotionToEmoji.put("Happiness", "😁");
        emotionToEmoji.put("Sadness", "😓");
        emotionToEmoji.put("Shame", "😶‍🌫️");
        emotionToEmoji.put("Surprise", "😮");
    }

    private void initializeUIComponents() {
        seekBar = findViewById(R.id.seekBarRadius);
        radiusText = findViewById(R.id.textRadiusValue);
        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        editSearch = findViewById(R.id.editSearch);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options_map, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterType.setAdapter(adapter);

        // Spinner selection listener
        spinnerFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterType = parent.getItemAtPosition(position).toString();
                updateMapMarkers(seekBar.getProgress());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Search text listener
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchKeyword = s.toString().toLowerCase();
                updateMapMarkers(seekBar.getProgress());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup SeekBar
        moodEventViewModel = MoodEventViewModel.getInstance();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(progress + " km");
                updateMapMarkers(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set initial user location
        Map<String, MoodEvent> moodEvents = moodEventViewModel.getMoodEvents();
        if (moodEvents != null && !moodEvents.isEmpty()) {
            MoodEvent firstEvent = moodEvents.values().iterator().next();
            userLocation = new LatLng(firstEvent.getLatitude(), firstEvent.getLongitude());
        } else {
            userLocation = new LatLng(53.5461, -113.4938); // Default to Edmonton
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupFirestoreQuery() {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> usernames = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String username = document.getString("username");
                    if (username != null) usernames.add(username);
                }
                Log.d("Usernames", usernames.toString());
            } else {
                Log.w("FirestoreError", "Error getting documents.", task.getException());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(seekBar.getProgress() * 1000)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)));
        updateMapMarkers(seekBar.getProgress());
    }

    private void updateMapMarkers(int radius) {
        if (mMap == null) return;

        mMap.clear();

        // Always maintain the radius circle (MOVED OUT OF CONDITIONAL)
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(radius * 1000)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)));

        if (currentFilterType.equals("Users")) {
            fetchUsersAndAddMarkers();
            return;
        }

        // Rest of existing mood event handling...
        Map<String, MoodEvent> moodEvents = moodEventViewModel.getMoodEvents();
        if (moodEvents == null || moodEvents.isEmpty()) return;

        for (MoodEvent event : moodEvents.values()) {
            try {
                LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                double distance = calculateDistance(userLocation, location);

                if (distance <= radius * 1000 && matchesFilterCriteria(event)) {
                    String emotionalState = event.getEmotionalState();
                    String emoji = emotionToEmoji.getOrDefault(emotionalState, "😶");

                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .icon(getEmojiBitmapDescriptor(emoji))
                            .title(emotionalState)
                            .snippet("Recorded at: " + event.getCreatedAt()));
                }
            } catch (Exception e) {
                Log.e("MAP_ERROR", "Error processing MoodEvent: " + e.getMessage());
            }
        }
    }

    private void fetchUsersAndAddMarkers() {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot userDoc : task.getResult()) {
                    String username = userDoc.getString("username");
                    List<Long> moodEventIds = (List<Long>) userDoc.get("MoodEventIds");
                    if (moodEventIds == null || moodEventIds.isEmpty()) {
                        continue;
                    }
                    Long firstEventId = moodEventIds.get(0);
                    db.collection("MoodEvents").document(String.valueOf(firstEventId))
                            .get().addOnCompleteListener(eventTask -> {
                                if (eventTask.isSuccessful()) {
                                    DocumentSnapshot eventDoc = eventTask.getResult();
                                    if (eventDoc.exists()) {
                                        Double lat = eventDoc.getDouble("latitude");
                                        Double lng = eventDoc.getDouble("longitude");
                                        String emotionalState = eventDoc.getString("emotionalState");

                                        if (lat != null && lng != null && emotionalState != null) {
                                            LatLng loc = new LatLng(lat, lng);
                                            String emoji = emotionToEmoji.getOrDefault(emotionalState, "😶");
                                            String snippet = "Emotional State: " + emoji + " " + emotionalState;

                                            runOnUiThread(() -> {
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(loc)
                                                        .icon(getEmojiBitmapDescriptor("👤")) // User emoji
                                                        .title(username)
                                                        .snippet(snippet));
                                            });
                                        }
                                    }
                                } else {
                                    Log.e("FirestoreError", "Error getting mood event", eventTask.getException());
                                }
                            });
                }
            } else {
                Log.e("FirestoreError", "Error getting users", task.getException());
            }
        });
    }

    private boolean matchesFilterCriteria(MoodEvent event) {
        if (currentSearchKeyword.isEmpty()) return true;

        String fieldValue = "";
        switch (currentFilterType) {
            case "Emotional State":
                fieldValue = event.getEmotionalState() != null ?
                        event.getEmotionalState().toLowerCase() : "";
                break;
            case "Reason":
                fieldValue = event.getExplanation() != null ?
                        event.getExplanation().toLowerCase() : "";
                break;
            case "Social Situation":
                fieldValue = event.getSocialSituation() != null ?
                        event.getSocialSituation().toLowerCase() : "";
                break;
        }
        return fieldValue.contains(currentSearchKeyword);
    }

    private BitmapDescriptor getEmojiBitmapDescriptor(String emoji) {
        Paint paint = new Paint();
        paint.setTextSize(100);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(emoji, 75, 100, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        Location location1 = new Location("point1");
        location1.setLatitude(point1.latitude);
        location1.setLongitude(point1.longitude);

        Location location2 = new Location("point2");
        location2.setLatitude(point2.latitude);
        location2.setLongitude(point2.longitude);

        return location1.distanceTo(location2);
    }
}


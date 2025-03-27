package com.example.moodster;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHandlerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng userLocation; // No longer hardcoded
    private MoodEventViewModel moodEventViewModel;
    private Circle radiusCircle;
    private HashMap<String, String> emotionToEmoji = new HashMap<>();

    // Get reference to your Firestore collection
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference usersRef = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_radius_mood_map);

        // Initialize emotion to emoji mappings
        emotionToEmoji.put("Anger", "😡");
        emotionToEmoji.put("Confusion", "😵‍💫");
        emotionToEmoji.put("Disgust", "🤢");
        emotionToEmoji.put("Fear", "😨");
        emotionToEmoji.put("Happiness", "😁");
        emotionToEmoji.put("Sadness", "😓");
        emotionToEmoji.put("Shame", "😶‍🌫️");
        emotionToEmoji.put("Surprise", "😮");

        // Initialize UI components
        SeekBar seekBar = findViewById(R.id.seekBarRadius);
        TextView radiusText = findViewById(R.id.textRadiusValue);

        moodEventViewModel = MoodEventViewModel.getInstance();
        Log.d("MOODS:", moodEventViewModel.getMoodEvents().toString());

        // Set user location based on first mood event or default
        Map<Integer, MoodEvent> moodEvents = moodEventViewModel.getMoodEvents();
        if (moodEvents != null && !moodEvents.isEmpty()) {
            MoodEvent firstEvent = moodEvents.values().iterator().next();
            userLocation = new LatLng(firstEvent.getLatitude(), firstEvent.getLongitude());
        } else {
            // Default to Edmonton coordinates if no events
            userLocation = new LatLng(53.5461, -113.4938);
        }

        radiusText.setText("2 km");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(progress + " km");
                updateMapMarkers(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> usernames = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String username = document.getString("username");
                    if (username != null) {
                        usernames.add(username);
                    }
                }
                // Now you have all usernames in the 'usernames' list
                Log.d("Usernames", usernames.toString());
                // Update your UI or do whatever you need with the list
            } else {
                Log.w("FirestoreError", "Error getting documents.", task.getException());
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));

        // Add initial circle
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(2 * 1000)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)));

        updateMapMarkers(2);
    }

    private void updateMapMarkers(int radius) {
        if (mMap == null) return;

        mMap.clear();

        // Always update the radius circle
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(radius * 1000)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)));

        // Get mood events and check if empty
        Map<Integer, MoodEvent> moodEvents = moodEventViewModel.getMoodEvents();
        if (moodEvents == null || moodEvents.isEmpty()) {
            Log.d("MAP", "No mood events to display");
            return; // Exit early but keep the map visible with radius circle
        }

        // Process mood events if available
        for (MoodEvent event : moodEvents.values()) {
            String emotionalState = event.getEmotionalState();
            String emoji = emotionToEmoji.getOrDefault(emotionalState, "😶");

            try {
                LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                double distance = calculateDistance(userLocation, location);

                if (distance <= radius * 1000) {
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
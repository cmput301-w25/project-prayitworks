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
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.moodster.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MapHandlerActivity extends AppCompatActivity implements OnMapReadyCallback{
    private GoogleMap mMap;
    private HashMap<String, LatLng> moodLocations = new HashMap<>();
    private LatLng userLocation = new LatLng(53.5461, -113.4938);
    private Circle radiusCircle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_radius_mood_map);

        // Initialize SeekBar and TextView
        SeekBar seekBar = findViewById(R.id.seekBarRadius);
        TextView radiusText = findViewById(R.id.textRadiusValue);

        radiusText.setText(2 + " km");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(progress + " km"); // Update radius text
                updateMapMarkers(progress); // Update markers and circle
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

        // Add example mood locations
        moodLocations.put("😠", new LatLng(53.5461, -113.4938)); // West Edmonton Mall
        moodLocations.put("😢", new LatLng(53.5354, -113.5070)); // University of Alberta
        moodLocations.put("😊", new LatLng(53.5322, -113.4907)); // Alberta Legislature Building
        moodLocations.put("🤩", new LatLng(53.5232, -113.5263)); // Rogers Place
        moodLocations.put("😌", new LatLng(53.5081, -113.5271)); // Hawrelak Park

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initial map setup
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f)); // Increased zoom level

        // Add initial circle
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(2 * 1000)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)));

        // Trigger initial marker update
        updateMapMarkers(2); // Initialize with 5km radius
    }

    private void updateMapMarkers(int radius) {
        if (mMap == null) return;

        mMap.clear(); // Clear existing markers and overlays

        // Re-add the circle with updated radius
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(radius * 1000) // Convert radius to meters
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)));

        // Add markers with emojis
        for (Map.Entry<String, LatLng> entry : moodLocations.entrySet()) {
            String mood = entry.getKey();
            LatLng location = entry.getValue();

            double distance = calculateDistance(userLocation, location);

            if (distance <= radius * 1000) {
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(getEmojiBitmapDescriptor(mood)) // Add emoji icon
                        .title(mood));
            }
        }
    }
    public BitmapDescriptor getEmojiBitmapDescriptor(String emoji) {
        // Create a paint object to draw the emoji
        Paint paint = new Paint();
        paint.setTextSize(100); // Adjust the size of the emoji
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        // Create a bitmap and canvas to draw the emoji
        Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888); // Adjust size as needed
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(emoji, 75, 100, paint); // Adjust position as needed

        // Convert the bitmap to a BitmapDescriptor
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    public double calculateDistance(LatLng point1, LatLng point2) {
        Location location1 = new Location("point1");
        location1.setLatitude(point1.latitude);
        location1.setLongitude(point1.longitude);

        Location location2 = new Location("point2");
        location2.setLatitude(point2.latitude);
        location2.setLongitude(point2.longitude);

        return location1.distanceTo(location2); // Distance in meters
    }
}

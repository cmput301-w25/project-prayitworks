package com.example.moodster;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MoodActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private Spinner spinnerSocialSituation;
    private EditText editTrigger, editExplanation;
    private Button btnSave, btnCancel, btnUploadImage;
    private ImageButton btnViewMoodHistory, btnSearch, btnHome, btnAddMood;

    private String selectedEmotionalState;
    private String currentUser;
    private Switch publicToggle;

    public static final List<String> VALID_SOCIAL_SITUATION = Arrays.asList(
            "Alone, with one other person",
            "With two to several people",
            "With a crowd"
    );

    private static final int MAX_IMAGE_SIZE = 65536;
    private Uri selectedImageUri = null;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedEmotionalState = getIntent().getStringExtra("mood");
        currentUser = getIntent().getStringExtra("username");

        if (currentUser == null || currentUser.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set correct layout for mood
        switch (Objects.requireNonNull(selectedEmotionalState)) {
            case "Anger": setContentView(R.layout.angry_mood); break;
            case "Confusion": setContentView(R.layout.confusion_mood); break;
            case "Disgust": setContentView(R.layout.disgust_mood); break;
            case "Sadness": setContentView(R.layout.sad_mood); break;
            case "Shame": setContentView(R.layout.shame_mood); break;
            case "Fear": setContentView(R.layout.fear_mood); break;
            case "Happiness": setContentView(R.layout.happy_mood); break;
            case "Surprise": setContentView(R.layout.surprise_mood); break;
            default: setContentView(R.layout.happy_mood); break;
        }

        moodEventViewModel = MoodEventViewModel.getInstance();
        moodEventViewModel.setUsername(currentUser);
        moodEventViewModel.setContext(this);

        editExplanation = findViewById(R.id.edit_reason);
        editTrigger = findViewById(R.id.edit_trigger);
        spinnerSocialSituation = findViewById(R.id.spinner_social);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnViewMoodHistory = findViewById(R.id.btn_calendar);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        publicToggle = findViewById(R.id.publicToggle);

        btnSearch = findViewById(R.id.btn_search);
        btnHome = findViewById(R.id.btn_home);
        btnAddMood = findViewById(R.id.btn_add);

        // Limit explanation and trigger fields to 20 characters
        editExplanation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        editTrigger.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        // Social spinner
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATION
        );
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSocialSituation.setAdapter(socialAdapter);

        // Image upload
        btnUploadImage.setOnClickListener(v -> openImageChooser());

        // Save mood
        btnSave.setOnClickListener(v -> {
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();
            String explanation = editExplanation.getText().toString().trim();

            boolean isPublic = publicToggle.isChecked();

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            getCurrentLocation((lat, lon) -> {
                latitude = lat;
                longitude = lon;
                Log.d("Location", "Lat=" + latitude + ", Lon=" + longitude);

                moodEventViewModel.addMoodEvent(
                        selectedEmotionalState,
                        trigger,
                        socialSituation,
                        explanation,
                        selectedImageUri,
                        latitude,
                        longitude,
                        isPublic,
                        new MoodEventViewModel.OnAddListener() {
                            @Override
                            public void onAddSuccess() {
                                Toast.makeText(MoodActivity.this, "Mood saved to Firestore!", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onAddFailure(String errorMessage) {
                                Toast.makeText(MoodActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                );

                Intent intentDone = new Intent(MoodActivity.this, AddMoodActivity.class);
                intentDone.putExtra("username", currentUser); // ✅ Pass username
                startActivity(intentDone);

                Log.d("MoodEvent Added!", moodEventViewModel.getMoodEvents().toString());
                Log.d("Location", "Lat=" + latitude + ", Lon=" + longitude);
            });
        });

        // View mood history
        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MoodActivity.this, MoodHistoryActivity.class);
            intent.putExtra("username", currentUser); // ✅
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MoodActivity.this, SearchUsersActivity.class);
            intent.putExtra("username", currentUser);
            startActivity(intent);
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MoodActivity.this, HomeActivity.class);
            intent.putExtra("username", currentUser);
            startActivity(intent);
        });

        btnAddMood.setOnClickListener(v -> {
            Intent intent = new Intent(MoodActivity.this, AddMoodActivity.class);
            intent.putExtra("username", currentUser);
            startActivity(intent);
        });

        // Cancel => close
        btnCancel.setOnClickListener(v -> finish());
    }

    // -----------------------------------
    // Image picking
    // -----------------------------------
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Uri uri = result.getData().getData();
                                if (uri != null) {
                                    processSelectedImage(uri);
                                }
                            }
                        }
                    });

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void processSelectedImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                byte[] imageBytes = new byte[inputStream.available()];
                int bytesRead = inputStream.read(imageBytes);
                inputStream.close();

                if (bytesRead > MAX_IMAGE_SIZE) {
                    Toast.makeText(this, "Image size exceeds 64 KB!", Toast.LENGTH_SHORT).show();
                } else {
                    selectedImageUri = imageUri;
                    Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
        }
    }

    // -----------------------------------
    // Location
    // -----------------------------------
    private void getCurrentLocation(LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    callback.onLocationReceived(latitude, longitude);
                } else {
                    callback.onLocationReceived(Double.NaN, Double.NaN); // Handle null case
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] perms,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, perms, grantResults);

        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Call getCurrentLocation with a callback to handle the result
            getCurrentLocation((latitude, longitude) -> {
                Log.d("Location", "Permission granted. Lat=" + latitude + ", Lon=" + longitude);
            });

        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}

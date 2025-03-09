package com.example.moodster;

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
import android.widget.TextView;
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
import com.google.firebase.Timestamp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.Manifest;

public class MoodActivity extends AppCompatActivity {

    private MoodEventViewModel moodEventViewModel;
    private Spinner spinnerEmotionalState;
    private Spinner spinnerSocialSituation;
    private EditText editTrigger, editSocialSituation, editExplanation;
    private Button btnSave, btnCancel, btnUploadImage;;
    private ImageButton btnViewMoodHistory;
    private String selectedEmotionalState; // Holds the selected state
    private TextView explanationCharCount;
    private ArrayAdapter<String> moodListAdapter;
    private List<MoodEvent> moodEventList = new ArrayList<>();

    public static final List<String> VALID_SOCIAL_SITUATION = Arrays.asList(
            "Alone, with one other person", "With two to several people", "With a crowd"
    );


    // Variable for uploading a photo
    private static final int MAX_IMAGE_SIZE = 65536; // 64 KB
    private Uri selectedImageUri = null; // Stores the selected image


    // Varaiable for location
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedEmotionalState = getIntent().getStringExtra("mood");

        if ("Anger".equals(selectedEmotionalState)) {
            setContentView(R.layout.angry_mood);
        } else if ("Sadness".equals(selectedEmotionalState)) {
            setContentView(R.layout.sad_mood);
        } else if ("Shame".equals(selectedEmotionalState)) {
            setContentView(R.layout.shame_mood);
        } else if ("Fear".equals(selectedEmotionalState)) {
            setContentView(R.layout.fear_mood);
        } else if ("Happiness".equals(selectedEmotionalState)) {
            setContentView(R.layout.happy_mood);
        }

        // Initialize ViewModel
        moodEventViewModel = MoodEventViewModel.getInstance();

        // Find views
        editExplanation = findViewById(R.id.edit_reason);
        //explanationCharCount = findViewById(R.id.textExplanationCount);
        editTrigger = findViewById(R.id.edit_trigger);

        spinnerSocialSituation = findViewById(R.id.spinner_social);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        btnViewMoodHistory = findViewById(R.id.btn_calendar);

        // For Photo
        btnUploadImage = findViewById(R.id.btn_upload_image);



        // Filter input field to 20 characters
        editExplanation.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        // TextWatcher: Update character count display
        /*
        editExplanation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                explanationCharCount.setText(s.length() + "/20"); // update TextView to char count
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });*/


        // Selecting Social Situation
        if (spinnerSocialSituation != null) { // Safety check
            ArrayAdapter<String> socialSituationAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, VALID_SOCIAL_SITUATION
            );
            socialSituationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSocialSituation.setAdapter(socialSituationAdapter);
        } else {
            Log.e("MoodActivity", "Spinner is null.");
        }

        // Attaching Photograph
        btnUploadImage.setOnClickListener(v -> openImageChooser());

        // Save button
        btnSave.setOnClickListener(v -> {
            String trigger = editTrigger.getText().toString().trim();
            String socialSituation = spinnerSocialSituation.getSelectedItem().toString();
            String explanation = editExplanation.getText().toString().trim();

            int id = 0;
            Timestamp timestamp = Timestamp.now();

            // Get the Location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            getCurrentLocation();
            Log.d("MoodEvent", "Location: " + latitude + longitude);

            //MoodEvent newMood = new MoodEvent(id, timestamp, selectedEmotionalState , trigger, socialSituation, explanation, selectedImageUri, longitude, latitude);
            //moodEventViewModel.addMoodEvent(newMood); // Adding to the Hashmap

            moodEventViewModel.addMoodEvent(timestamp, selectedEmotionalState , trigger, socialSituation, explanation, selectedImageUri, longitude, latitude);
            finish();

            Log.d("MoodEvent", "All Moods: " + moodEventList);
        });

        btnViewMoodHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MoodActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> {
            finish();
        });

    }
    /////////////////////
    // Activity result handler for image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                selectedImageUri = result.getData().getData();
                                if (selectedImageUri != null) {
                                    processSelectedImage(selectedImageUri);
                                }
                            }
                        }
                    });

    // Open gallery to choose an image
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Process the selected image and check size
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
                    selectedImageUri = imageUri; // Save image URI for later use
                    Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
        }
    }
    //////////

    /// LOCATION //
    // Fetch the user's current location
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d("Location", "Lat: " + latitude + ", Lon: " + longitude);
                }
            });

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

}
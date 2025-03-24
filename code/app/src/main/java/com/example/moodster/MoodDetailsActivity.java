package com.example.moodster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MoodDetailsActivity extends AppCompatActivity {
    private TextView textMoodEmoji, textMoodDateTime, textReasonValue, textTriggerValue, textSocialValue;
    private ImageView imageMoodPhoto;


    private int MoodEventPos;
    private MoodEventViewModel moodEventViewModel;
    private Button btnDeleteMood, btnEditMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_press_view_details_mood_history);

        moodEventViewModel = MoodEventViewModel.getInstance();

        Intent intent = getIntent();

        textMoodEmoji = findViewById(R.id.textMoodEmoji);
        textMoodDateTime = findViewById(R.id.textMoodDateTime);
        textReasonValue = findViewById(R.id.textReasonValue);
        textTriggerValue = findViewById(R.id.textTriggerValue);
        textSocialValue = findViewById(R.id.textSocialValue);
        imageMoodPhoto = findViewById(R.id.imageMoodPhoto);
        textMoodEmoji.setText(intent.getStringExtra("textMoodEmoji"));
        textMoodDateTime.setText(String.valueOf(intent.getStringExtra("textMoodDateTime")));
        textReasonValue.setText(intent.getStringExtra("textReasonValue"));
        textTriggerValue.setText(intent.getStringExtra("textTriggerValue"));
        textSocialValue.setText(intent.getStringExtra("textSocialValue"));
        imageMoodPhoto.setImageURI(intent.getParcelableExtra("imageMoodPhoto"));
        MoodEventPos = intent.getIntExtra("position",0);

        List<Integer> keys = new ArrayList<>(moodEventViewModel.getMoodEvents().keySet());
        int key = keys.get(MoodEventPos);
        MoodEvent event = moodEventViewModel.getMoodEvents().get(key);

        // Deleting a mood
        btnDeleteMood = findViewById(R.id.buttonDelete);
        btnDeleteMood.setOnClickListener(v -> {
            //Log.d("POS", String.valueOf(MoodEventPos));
            //Log.d("IWANT", moodEventViewModel.getMoodEvents().toString());
            int keyDlt = 0;
            
            for(int i = 0; i <= MoodEventPos; i++){
                if(MoodEventPos == i){
                    keyDlt = keys.get(i);
                }
            }
            moodEventViewModel.getMoodEvents().remove(keyDlt);
            //Log.d("IWANT", moodEventViewModel.getMoodEvents().toString());
            //setContentView(R.layout.mood_history_main);
            // Going back to mood History
            Intent intentDone = new Intent(MoodDetailsActivity.this, MoodHistoryActivity.class);
            startActivity(intentDone);
            //moodEventViewModel.getMoodEvents();
        });

        // Editing a mood
        btnEditMood = findViewById(R.id.buttonEdit);
        btnEditMood.setOnClickListener(v -> {
            Intent intentEdit = new Intent(MoodDetailsActivity.this, EditMoodActivity.class);
            intentEdit.putExtra("position", MoodEventPos);
            startActivity(intentEdit);
        });
    }
}

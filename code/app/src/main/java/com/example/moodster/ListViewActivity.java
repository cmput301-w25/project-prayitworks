package com.example.moodster;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListViewActivity extends AppCompatActivity {
    private ListView listView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> moodEventList;
    private MoodEventViewModel moodEventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_event_view);

        listView = findViewById(R.id.listView);
        moodEventViewModel = MoodEventViewModel.getInstance();

        // **Retrieve Mood Events from Singleton HashMap**
        Map<Integer, MoodEvent> moodEventsMap = moodEventViewModel.getMoodEvents();
        moodEventList = new ArrayList<>(moodEventsMap.values());

        // **Ensure list is updated properly**
        if (moodEventList.isEmpty()) {
            System.out.println("No mood events found!");
        }

        // Set up adapter
        adapter = new MoodEventAdapter(this, moodEventList);
        listView.setAdapter(adapter);
    }
}
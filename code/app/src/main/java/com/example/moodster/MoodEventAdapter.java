package com.example.moodster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import java.util.List;

public class MoodEventAdapter extends BaseAdapter {
    private Context context;
    private List<MoodEvent> moodEvents;

    public MoodEventAdapter(Context context, List<MoodEvent> moodEvents) {
        this.context = context;
        this.moodEvents = moodEvents;
    }

    @Override
    public int getCount() {
        return moodEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return moodEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mood_event_list_item, parent, false);
        }

        MoodEvent moodEvent = moodEvents.get(position);

        TextView textViewReason = convertView.findViewById(R.id.textViewReason);
        TextView textViewTrigger = convertView.findViewById(R.id.textViewTrigger);
        TextView textViewSocial = convertView.findViewById(R.id.textViewSocial);
        TextView textViewDateTime = convertView.findViewById(R.id.textViewDateTime);
        Button buttonViewDetails = convertView.findViewById(R.id.buttonViewDetails);

        textViewReason.setText("Emotion: " + moodEvent.getEmotionalState());
        textViewTrigger.setText("Trigger: " + moodEvent.getTrigger());
        textViewSocial.setText("Social Situation: " + moodEvent.getSocialSituation());
        textViewDateTime.setText("Timestamp: " + moodEvent.getTimestamp());

        buttonViewDetails.setOnClickListener(v -> {
            // placeholder for event detail view screen
            Toast.makeText(context, "Viewing details for: " + moodEvent.getEmotionalState(), Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}
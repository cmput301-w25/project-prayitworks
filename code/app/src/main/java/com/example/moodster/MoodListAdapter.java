package com.example.moodster;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class MoodListAdapter extends BaseAdapter {
    private Context context;
    private List<MoodEvent> moodEvents;

    public MoodListAdapter(Context context, List<MoodEvent> moodEvents) {
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

    public void updateList(List<MoodEvent> newList) {
        this.moodEvents = newList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mood_event_list_item, parent, false);
        }
        MoodEvent moodEvent = moodEvents.get(position);

        TextView textViewEmoji = convertView.findViewById(R.id.textViewMood);
        TextView textViewMoodPreview = convertView.findViewById(R.id.textViewMoodPreview);
        Button buttonViewDetails = convertView.findViewById(R.id.buttonViewDetails);

        textViewEmoji.setText(moodEvent.getEmoji());
        textViewMoodPreview.setText(
                "Reason: " + moodEvent.getExplanation() + "\n" +
                        "Trigger: " + moodEvent.getTrigger() + "\n" +
                        "Social Situation: " + moodEvent.getSocialSituation()
        );

        buttonViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, MoodDetailsActivity.class);
            intent.putExtra("textMoodEmoji", moodEvent.getEmoji());
            intent.putExtra("textMoodDateTime", moodEvent.getCreatedAt());
            intent.putExtra("textReasonValue", moodEvent.getExplanation());
            intent.putExtra("textTriggerValue", moodEvent.getTrigger());
            intent.putExtra("textSocialValue", moodEvent.getSocialSituation());
            intent.putExtra("imageMoodPhoto", moodEvent.getImage());
            intent.putExtra("moodId", moodEvent.getId());
            context.startActivity(intent);
        });

        return convertView;
    }
}

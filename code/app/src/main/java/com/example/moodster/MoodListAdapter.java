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

/**
 * The MoodListAdapter class adapts a list of MoodEvent objects for display in a ListView. Each
 * list item shows an emoji representing the mood, a preview of the mood details, and a button to
 * view detailed information about the mood event.
 */
public class MoodListAdapter extends BaseAdapter {
    private Context context;
    private List<MoodEvent> moodEvents;
    private String currentUsername;

    /**
     * MoodListAdapter Constructor
     *
     * @param context
     *      the Context in which the adapter is running
     * @param moodEvents
     *      he list of MoodEvent objects to display
     * @param currentUsername
     *      the username of the current user
     */
    public MoodListAdapter(Context context, List<MoodEvent> moodEvents, String currentUsername) {
        this.context = context;
        this.moodEvents = moodEvents;
        this.currentUsername = currentUsername;
    }

    /**
     * Returns the number of mood events in the list.
     *
     * @return
     *      the size of the moodEvents list
     */
    @Override
    public int getCount() {
        return moodEvents.size();
    }

    /**
     * Returns the MoodEvent at the specified position.
     *
     * @param position
     *      the position of the item in the list
     * @return
     *      the MoodEvent object at the specified position
     */
    @Override
    public Object getItem(int position) {
        return moodEvents.get(position);
    }

    /**
     * Returns the item ID for the specified position.
     *
     * @param position
     *      the position of the item in the list
     * @return
     *      the position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Updates the list of mood events and notifies the ListView of data changes.
     *
     * @param newList
     *      the new list of MoodEvent objects
     */
    public void updateList(List<MoodEvent> newList) {
        this.moodEvents = newList;
        notifyDataSetChanged();
    }

    /**
     * Returns the view for the mood event at the specified position.
     *
     * <p>This method inflates the layout for each list item if necessary, binds the mood event data
     * (emoji and mood preview) to the views, and sets a click listener on the "View Details" button
     * to navigate to MoodDetailsActivity.</p>
     *
     * @param position
     *      the position of the mood event in the list
     * @param convertView
     *      the recycled view to populate, if available
     * @param parent
     *      the parent view that this view will eventually be attached to
     * @return
     *      the View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mood_event_list_item, parent, false);
        }

        MoodEvent moodEvent = moodEvents.get(position);

        TextView textViewEmoji = convertView.findViewById(R.id.textViewMood);
        TextView textViewMoodPreview = convertView.findViewById(R.id.textViewMoodPreview);
        Button buttonViewDetails = convertView.findViewById(R.id.buttonViewDetails);

        // Set the text for emoji and preview
        textViewEmoji.setText(moodEvent.getEmoji());
        textViewMoodPreview.setText(
                "Reason: " + moodEvent.getExplanation() + "\n" +
                        "Trigger: " + moodEvent.getTrigger() + "\n" +
                        "Social Situation: " + moodEvent.getSocialSituation()
        );

        // Set the details button click listener
        buttonViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, MoodDetailsActivity.class);
            intent.putExtra("textMoodEmoji", moodEvent.getEmoji());
            intent.putExtra("textMoodDateTime", moodEvent.getCreatedAt());
            intent.putExtra("textReasonValue", moodEvent.getExplanation());
            intent.putExtra("textTriggerValue", moodEvent.getTrigger());
            intent.putExtra("textSocialValue", moodEvent.getSocialSituation());
            intent.putExtra("textVisibilityValue", moodEvent.isPublic() ? "Public" : "Private");
            intent.putExtra("imageMoodPhoto", moodEvent.getImage());
            intent.putExtra("moodId", moodEvent.getMoodId());
            intent.putExtra("username", currentUsername);

            context.startActivity(intent);
        });

        // Fade-in effect when the item is loaded
        convertView.setAlpha(0f);
        convertView.animate().alpha(1f).setDuration(1000);

        return convertView;
    }
}

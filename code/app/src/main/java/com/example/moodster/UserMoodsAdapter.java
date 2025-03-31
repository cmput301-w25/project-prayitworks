package com.example.moodster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing a list of MoodEvents in the UserProfileActivity's RecyclerView.
 * Uses item_network_mood_feed.xml as the row layout.
 */
public class UserMoodsAdapter extends RecyclerView.Adapter<UserMoodsAdapter.MoodViewHolder> {

    private List<MoodEvent> moodList = new ArrayList<>();

    public void setMoods(List<MoodEvent> newList) {
        moodList = newList;
        notifyDataSetChanged();
    }

    public void addMood(MoodEvent mood) {
        moodList.add(mood);
        notifyItemInserted(moodList.size() - 1);
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_network_mood_feed, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodEvent mood = moodList.get(position);

        // imageUserAvatarItem, textUserNameItem, textMoodCommentItem, textMoodEmojiItem, textMoodTimeItem, iconLikeItem, iconCommentItem
        holder.textUserName.setText(mood.getEmotionalState());
        holder.textMoodComment.setText(mood.getExplanation());
        holder.textMoodEmoji.setText(mood.getEmoji());
        holder.textMoodTime.setText(mood.getCreatedAt().toDate().toString());

    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar, iconLike, iconComment;
        TextView textUserName, textMoodComment, textMoodEmoji, textMoodTime;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageUserAvatarItem);
            textUserName = itemView.findViewById(R.id.textUserNameItem);
            textMoodComment = itemView.findViewById(R.id.textMoodCommentItem);
            textMoodEmoji = itemView.findViewById(R.id.textMoodEmojiItem);
            textMoodTime = itemView.findViewById(R.id.textMoodTimeItem);
            iconLike = itemView.findViewById(R.id.iconLikeItem);
            iconComment = itemView.findViewById(R.id.iconCommentItem);
        }
    }
}

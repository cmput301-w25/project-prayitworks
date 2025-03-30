package com.example.moodster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<SearchUser> friendList;

    public FriendListAdapter(List<SearchUser> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListAdapter.ViewHolder holder, int position) {
        SearchUser friend = friendList.get(position);
        holder.textDisplayName.setText(friend.getDisplayName());
        holder.textUsername.setText("@" + friend.getUsername());

        // Hide the "add friend" icon since these users are already friends.
        holder.imageAddFriend.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDisplayName, textUsername;
        ImageView imageAvatar, imageAddFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDisplayName = itemView.findViewById(R.id.textDisplayName);
            textUsername = itemView.findViewById(R.id.textUsername);
            imageAvatar = itemView.findViewById(R.id.imageAvatar);
            imageAddFriend = itemView.findViewById(R.id.imageAddFriend);
        }
    }
}

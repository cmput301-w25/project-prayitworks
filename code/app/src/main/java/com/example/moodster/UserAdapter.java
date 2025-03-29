package com.example.moodster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<SearchUser> SearchUsers = new ArrayList<>();

    public void setUsers(List<SearchUser> SearchUsers) {
        this.SearchUsers = SearchUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchUser user = SearchUsers.get(position);
        holder.textDisplayName.setText(user.getDisplayName());
        holder.textUsername.setText("@" + user.getUsername());

        holder.imageAddFriend.setOnClickListener(v -> {
            // Handle add friend logic
        });
    }

    @Override
    public int getItemCount() {
        return SearchUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDisplayName, textUsername;
        ImageView imageAddFriend;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDisplayName = itemView.findViewById(R.id.textDisplayName);
            textUsername = itemView.findViewById(R.id.textUsername);
            imageAddFriend = itemView.findViewById(R.id.imageAddFriend);
        }
    }
}

package com.example.moodster;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<SearchUser> SearchUsers = new ArrayList<>();
    private String currentUsername;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setUsers(List<SearchUser> SearchUsers) {
        this.SearchUsers = SearchUsers;
        notifyDataSetChanged();
    }

    // Constructor to accept the current user's username
    public UserAdapter(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_add, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchUser user = SearchUsers.get(position);
        holder.textDisplayName.setText(user.getDisplayName());
        holder.textUsername.setText("@" + user.getUsername());

        holder.addUserBtn.setOnClickListener(v -> {
            String targetUsername = user.getUsername();
            Context context = v.getContext();

            // checking if current user already follows target user
            db.collection("Users").document(currentUsername).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> following = (List<String>) documentSnapshot.get("following");
                    if (following != null && following.contains(targetUsername)) {
                        Toast.makeText(context, "You already follow this user.", Toast.LENGTH_SHORT).show();
                    } else {
                        // confirmation pop up to send follow request
                        new AlertDialog.Builder(context)
                                .setTitle("Follow Request")
                                .setMessage("Do you want to send a follow request to " + user.getDisplayName() + "?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    // Update the target user's followRequests field
                                    db.collection("Users").document(targetUsername)
                                            .update("followRequests", FieldValue.arrayUnion(currentUsername))
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(context, "Follow request sent.", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(context, "error checking follow status", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return SearchUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDisplayName, textUsername;
        ImageView imageAddFriend;
        Button addUserBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDisplayName = itemView.findViewById(R.id.textDisplayName);
            textUsername = itemView.findViewById(R.id.textUsername);
            addUserBtn = itemView.findViewById(R.id.addUserBtn);
            //imageAddFriend = itemView.findViewById(R.id.imageAddFriend);
        }
    }
}

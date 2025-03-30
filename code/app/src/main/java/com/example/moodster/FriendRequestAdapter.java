package com.example.moodster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    // List of usernames who sent friend requests
    private List<String> requestList;
    // The current user's username
    private String currentUsername;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FriendRequestAdapter(List<String> requestList, String currentUsername) {
        this.requestList = requestList;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_accept_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String requesterUsername = requestList.get(position);
        Context context = holder.itemView.getContext();

        holder.textDisplayName.setText(requesterUsername);
        holder.textUsername.setText("sent you a follow request");

        holder.buttonAccept.setOnClickListener(v -> {
            db.collection("Users").document(currentUsername)
                    // remove requester from current user followRequests.
                    .update("followRequests", FieldValue.arrayRemove(requesterUsername))
                    .addOnSuccessListener(aVoid -> {
                        db.collection("Users").document(currentUsername)
                                // add requester to current user's followers.
                                .update("followers", FieldValue.arrayUnion(requesterUsername))
                                .addOnSuccessListener(aVoid2 -> {
                                    db.collection("Users").document(requesterUsername)
                                            // add current user to the requester's following.
                                            .update("following", FieldValue.arrayUnion(currentUsername))
                                            .addOnSuccessListener(aVoid3 -> {
                                                Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                                requestList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, requestList.size());
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(context, "Error updating requester", Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error updating followers", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error removing follow request", Toast.LENGTH_SHORT).show());
        });

        holder.buttonDecline.setOnClickListener(v -> {
            // remove the request from the followRequests field.
            db.collection("Users").document(currentUsername)
                    .update("followRequests", FieldValue.arrayRemove(requesterUsername))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
                        requestList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, requestList.size());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error declining request", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Assuming you have added these IDs in your XML.
        TextView textDisplayName, textUsername;
        Button buttonDecline, buttonAccept;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDisplayName = itemView.findViewById(R.id.textDisplayName);
            textUsername = itemView.findViewById(R.id.textUsername);
            buttonDecline = itemView.findViewById(R.id.buttonDecline);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
        }
    }
}

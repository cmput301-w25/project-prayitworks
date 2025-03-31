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


/**
 * The FriendRequestAdapter class binds a list of friend requests to the view items. It provides
 * functionality to accept or decline each friend request. Accepting a request removes the request
 * from the current user's followRequests and adds the requester to the current user's followers.
 * It also adds the current user to the requester's following. Declining a request just removes the request.
 */
public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    // List of usernames who sent friend requests
    private List<String> requestList;
    // The current user's username
    private String currentUsername;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * FriendRequestAdapter Constructor
     *
     * @param requestList
     *      the list of usernames who have sent friend requests
     * @param currentUsername
     *      the current user's username
     */
    public FriendRequestAdapter(List<String> requestList, String currentUsername) {
        this.requestList = requestList;
        this.currentUsername = currentUsername;
    }

    /**
     * Inflates the view for an individual friend request item.
     *
     * @param parent
     *      the parent ViewGroup into which the new view will be added
     * @param viewType
     *      the view type of the new view (unused)
     * @return
     *      a new ViewHolder containing the inflated view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_accept_request, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder for a friend request.
     *
     * <p>This method sets the display name and status message for each friend request. It also sets click listeners
     * for the accept and decline buttons. When a request is accepted, the adapter updates the Firebase database by
     * removing the requester from the followRequests field of the current user, adding the requester to the current user's
     * followers, and adding the current user to the requester's following list. When a request is declined, the request
     * is removed from the followRequests field.</p>
     *
     * @param holder
     *      the ViewHolder to bind data to
     * @param position
     *      the position of the item in the adapter
     */
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

    /**
     * Returns the total number of friend requests.
     *
     * @return the number of friend requests in the list
     */
    @Override
    public int getItemCount() {
        return requestList.size();
    }

    /**
     * ViewHolder class for friend request items.
     *
     * <p>This inner class holds references to the UI components for each friend request item,
     * including the display name, a status message, and the accept/decline buttons.</p>
     */
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

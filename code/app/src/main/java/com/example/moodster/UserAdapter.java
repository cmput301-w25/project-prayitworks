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
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * The UserAdapter class adapts a list of SearchUser objects for display in a RecyclerView. Each list item shows
 * the user's display name and username, and allows the current user to view the target user's profile
 * or send a follow request via a confirmation dialog.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<SearchUser> SearchUsers = new ArrayList<>();
    private String currentUsername;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Sets the list of SearchUser objects to be displayed.
     *
     * @param SearchUsers
     *      a List of SearchUser objects
     */
    public void setUsers(List<SearchUser> SearchUsers) {
        this.SearchUsers = SearchUsers;
        notifyDataSetChanged();
    }

    /**
     * UserAdapter Constructor
     *
     * @param currentUsername
     *      the username of the current user; used for follow request logic
     */
    public UserAdapter(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    /**
     * Inflates the view for an individual user list item.
     *
     * @param parent
     *      the parent ViewGroup
     * @param viewType
     *      the view type (unused)
     * @return
     *      a new ViewHolder containing the inflated layout
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_add, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a SearchUser object to the list item view.
     *
     * <p>This method sets the display name and username text. It sets a click listener on the entire item
     * to launch UserProfileActivity, and another on the Add User button to send a follow request.
     * The follow request button checks if the current user already follows the target user,
     * and if not, prompts the user for confirmation before sending a follow request.</p>
     *
     * @param holder
     *      the ViewHolder for the item
     * @param position
     *      the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchUser user = SearchUsers.get(position);
        holder.textDisplayName.setText(user.getDisplayName());
        holder.textUsername.setText("@" + user.getUsername());

        // NEW: Launch the UserProfileActivity on row click
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, UserProfileActivity.class);
            // Pass along basic data; the rest can be fetched in the activity
            intent.putExtra("username", user.getUsername());
            intent.putExtra("displayName", user.getDisplayName());
            context.startActivity(intent);
        });

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

    /**
     * Returns the total number of user items in the list.
     *
     * @return
     *      the size of the SearchUsers list
     */
    @Override
    public int getItemCount() {
        return SearchUsers.size();
    }

    /**
     * ViewHolder class that holds the views for each user list item.
     */
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

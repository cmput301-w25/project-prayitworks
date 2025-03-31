package com.example.moodster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The FriendListAdapter class binds a list of SearchUser objects to the item_user layout, showing
 * each friend's display name and username.</p>
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<SearchUser> friendList;

    /**
     * FriendListAdapter Constructor.
     *
     * @param friendList
     *      the list of SearchUser objects representing the friends to display
     */
    public FriendListAdapter(List<SearchUser> friendList) {
        this.friendList = friendList;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent
     *      the parent ViewGroup into which the new view will be added after it is bound to an adapter position
     * @param viewType
     *      the view type of the new View
     * @return
     *      a new ViewHolder that holds a View of type item_user
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * <p>This method binds the SearchUser object data to the corresponding views in the ViewHolder.
     * It sets the display name and username.</p>
     *
     * @param holder
     *      the ViewHolder which should be updated to represent the contents of the item at the given position
     * @param position
     *      the position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchUser friend = friendList.get(position);
        holder.textDisplayName.setText(friend.getDisplayName());
        holder.textUsername.setText("@" + friend.getUsername());

        // Hide the "add friend" icon since these users are already friends.
        holder.imageAddFriend.setVisibility(View.GONE);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return
     *      the size of the friend list
     */
    @Override
    public int getItemCount() {
        return friendList.size();
    }

    /**
     * ViewHolder class for friend list items.
     *
     * <p>This class holds references to the UI components for a friend item view,
     * including the display name, username, avatar, and add friend icon.</p>
     */
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

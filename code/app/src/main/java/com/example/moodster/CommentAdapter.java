package com.example.moodster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * The CommentAdapter class provides a read-only adapter for rendering a list of comments in the
 * Moodster app.
 */
public class CommentAdapter extends BaseAdapter {
    private final Context context;
    private final List<CommentItem> commentList;
    private final String currentUsername;
    private final String moodEventId;

    /**
     * Constructor for a new CommentAdapter.
     *
     * @param context
     *      the Context where the adapter is running
     * @param commentList
     *      a List of CommentItem objects to be displayed
     * @param currentUsername
     *      the username of the current user
     * @param moodEventId
     *      the identifier of the mood event associated with these comments
     */
    public CommentAdapter(Context context, List<CommentItem> commentList,
                          String currentUsername, String moodEventId) {
        this.context = context;
        this.commentList = commentList;
        this.currentUsername = currentUsername;
        this.moodEventId = moodEventId;
    }

    /**
     * Returns the number of comments in the adapter.
     *
     * @return
     *      the size of the commentList
     */
    @Override
    public int getCount() {
        return commentList.size();
    }

    /**
     * Returns the CommentItem at the specified position.
     *
     * @param position
     *      the index of the comment to return
     * @return
     *      the CommentItem object at the specified position
     */
    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    /**
     * Returns the id of the item.
     *
     * @param position
     *      the index of the item
     * @return
     *      the position of the item as its id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Renders the view for each comment item in the list.
     *
     * <p>
     * This method inflates the comment_list_item layout and binds the data from the CommentItem
     * at the given position to the corresponding views. It then formats the timestamp for display.
     * </p>
     *
     * @param position
     *      the index of the displayed comment
     * @param convertView
     *      the recycled view to populate
     * @param parent
     *      the parent view that this view will be attached to
     * @return
     *      the View corresponding to the data at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.comment_list_item, parent, false);
        }

        CommentItem comment = commentList.get(position);

        TextView usernameText = convertView.findViewById(R.id.commentUsername);
        TextView commentText = convertView.findViewById(R.id.commentText);
        TextView timestampText = convertView.findViewById(R.id.commentTimestamp);

        usernameText.setText(comment.getUsername());
        commentText.setText(comment.getCommentText());

        // Format the timestamp
        Timestamp ts = comment.getTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        timestampText.setText(sdf.format(ts.toDate()));

        return convertView;
    }
}

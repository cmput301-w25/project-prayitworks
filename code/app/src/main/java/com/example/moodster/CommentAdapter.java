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
 * Basic read-only adapter.
 * If you want editing/deleting, you'll adapt the new array-of-objects structure
 */
public class CommentAdapter extends BaseAdapter {
    private final Context context;
    private final List<CommentItem> commentList;
    private final String currentUsername;
    private final String moodEventId;

    public CommentAdapter(Context context, List<CommentItem> commentList,
                          String currentUsername, String moodEventId) {
        this.context = context;
        this.commentList = commentList;
        this.currentUsername = currentUsername;
        this.moodEventId = moodEventId;
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Renders each comment in comment_list_item.xml
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

        // If you do want edit/delete, you can show/hide buttons here
        // using something like:
        // if (comment.getUsername().equals(currentUsername)) { ... }

        return convertView;
    }
}

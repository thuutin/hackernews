package me.tintran.hackernews.storydetail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import me.tintran.hackernews.R;

/**
 * Created by tin on 7/7/16.
 */
class CommentItemViewHolder extends RecyclerView.ViewHolder {
  TextView commentTextView;
  TextView commentIdTextView;

  CommentItemViewHolder(View itemView) {
    super(itemView);
    commentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
    commentIdTextView = (TextView) itemView.findViewById(R.id.commentId);
  }
}

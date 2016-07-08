package me.tintran.hackernews.storydetail;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.List;
import me.tintran.hackernews.R;

/**
 * Created by tin on 7/7/16.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentItemViewHolder> {

  private List<Comment> comments;

  @Override public CommentItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new CommentItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false));
  }


  @Override public void onBindViewHolder(CommentItemViewHolder holder, int position) {
    Comment comment = comments.get(position);
    holder.commentTextView.setText(comment.text);
  }

  @Override public int getItemCount() {
    return comments == null ? 0 : comments.size();
  }

  public void swapData(List<Comment> comments) {
    this.comments = comments;
    notifyDataSetChanged();
  }
}

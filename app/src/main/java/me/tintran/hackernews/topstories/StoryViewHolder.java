package me.tintran.hackernews.topstories;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.TwoLineListItem;
import me.tintran.hackernews.R;

/**
 * Created by tin on 7/6/16.
 */
class StoryViewHolder extends RecyclerView.ViewHolder {
  public TextView titleTextView;
  public TextView subtitleTextView;

  public StoryViewHolder(View itemView) {
    super(itemView);
    titleTextView = (TextView) itemView.findViewById(R.id.title);
    subtitleTextView = (TextView) itemView.findViewById(R.id.subtitle);
  }
}

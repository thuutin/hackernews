package me.tintran.hackernews.topstories;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Story;

/**
 * Created by tin on 7/6/16.
 */
class StoryViewHolder extends RecyclerView.ViewHolder {
  public TextView titleTextView;
  public TextView subtitleTextView;
  public Story story;

  public StoryViewHolder(View itemView, final StoriesAdapter.OnStoryClick onStoryClick) {
    super(itemView);
    titleTextView = (TextView) itemView.findViewById(R.id.title);
    subtitleTextView = (TextView) itemView.findViewById(R.id.subtitle);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        onStoryClick.onClick(story);
      }
    });
  }
}

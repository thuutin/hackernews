package me.tintran.hackernews.topstories;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Story;

/**
 * Created by tin on 7/6/16.
 */

class StoriesAdapter extends RecyclerView.Adapter<StoryViewHolder> {

  private List<Story> storyList;
  private OnStoryClick onStoryClick;

  StoriesAdapter(OnStoryClick onStoryClick) {
    this.onStoryClick = onStoryClick;
  }

  @Override public StoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_item, parent, false);
    return new StoryViewHolder(itemView, onStoryClick);
  }

  @Override public void onBindViewHolder(StoryViewHolder holder, int position) {
    final Story story = storyList.get(position);
    holder.titleTextView.setText(story.title);
    final String timeString = DateUtils.getRelativeTimeSpanString(holder.subtitleTextView.getContext(), story.time).toString();
    holder.subtitleTextView.setText(story.score + " scores" + "    " + timeString + "    by: " + story.by);
    holder.story = story;
  }

  @Override public int getItemCount() {
    if (storyList == null){
      return 0;
    }
    return storyList.size();
  }

  @Override public long getItemId(int position) {
    return storyList.get(position).id;
  }

  void swapData(List<Story> itemsList) {
    this.storyList = itemsList;
    notifyDataSetChanged();
  }

  interface OnStoryClick {

    void onClick(Story story);

  }
}

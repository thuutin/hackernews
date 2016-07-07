package me.tintran.hackernews.topstories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Item;

/**
 * Created by tin on 7/6/16.
 */

class StoriesAdapter extends RecyclerView.Adapter<StoryViewHolder> {

  private List<Item> itemList;
  private OnStoryClick onStoryClick;

  StoriesAdapter(OnStoryClick onStoryClick) {
    this.onStoryClick = onStoryClick;
  }

  @Override public StoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_item, parent, false);
    return new StoryViewHolder(itemView, onStoryClick);
  }

  @Override public void onBindViewHolder(StoryViewHolder holder, int position) {
    final Item item = itemList.get(position);
    holder.titleTextView.setText(item.title);
    holder.subtitleTextView.setText(item.subtitle);
    holder.item = item;
  }

  @Override public int getItemCount() {
    if (itemList == null){
      return 0;
    }
    return itemList.size();
  }

  void swapData(List<Item> itemsList) {
    this.itemList = itemsList;
    notifyDataSetChanged();
  }

  interface OnStoryClick {

    void onClick(Item item);

  }
}

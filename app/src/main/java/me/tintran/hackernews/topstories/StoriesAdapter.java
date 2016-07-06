package me.tintran.hackernews.topstories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import me.tintran.hackernews.R;

/**
 * Created by tin on 7/6/16.
 */

public class StoriesAdapter extends RecyclerView.Adapter<StoryViewHolder> {
  private List<Item> itemList;
  public StoriesAdapter() {
  }

  @Override public StoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_item, parent, false);
    return new StoryViewHolder(itemView);
  }

  @Override public void onBindViewHolder(StoryViewHolder holder, int position) {

  }

  @Override public int getItemCount() {
    if (itemList == null){
      return 0;
    }
    return itemList.size();
  }

  public void swapData(List<Item> itemsList) {
    this.itemList = itemsList;
    notifyDataSetChanged();
  }
}

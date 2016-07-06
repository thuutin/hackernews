package me.tintran.hackernews.topstories;

import java.util.List;

/**
 * Created by tin on 7/6/16.
 */

interface TopStoriesUseCase {
  void getTopStories(Callback callback);

  interface Callback {
    void onComplete(List<Item> items);
    void onError(int code);
  }
}

package me.tintran.hackernews.data;

import java.util.List;

/**
 * Created by tin on 7/6/16.
 */

public interface TopStoriesUseCase {

  void getTopStories(Callback callback);

  interface Callback {
    void onComplete(List<Story> stories);
    void onError(int code);
  }
}

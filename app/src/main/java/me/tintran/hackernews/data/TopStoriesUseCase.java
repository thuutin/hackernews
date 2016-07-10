package me.tintran.hackernews.data;

import java.util.List;

/**
 * Created by tin on 7/6/16.
 */

public interface TopStoriesUseCase {

  public static final int NO_COMMENT = 2000;
  public static final int LOADING_COMMENTS = 2001;

  void getTopStories(Callback callback);

  interface Callback {
    void onComplete(List<Story> stories);
    void onError(int code);
  }
}

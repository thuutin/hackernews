package me.tintran.hackernews;

/**
 * Created by tin on 7/9/16.
 */
public interface StopListener {
  void notifyStop();

  void notifyLoadingCommentComplete(int storyId);

  void notifyLoadingTopStoryComplete();
}

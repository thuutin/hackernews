package me.tintran.hackernews.sync;

/**
 * Created by tin on 7/9/16.
 */
public interface ServiceHandlerInteraction {
  void notifyStop();

  void notifyLoadingCommentComplete(int storyId);

  void notifyLoadingTopStoryComplete();
}

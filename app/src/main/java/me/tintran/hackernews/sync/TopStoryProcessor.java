package me.tintran.hackernews.sync;

import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.StoryCommentGateway;
import me.tintran.hackernews.StoryGateway;
import me.tintran.hackernews.TopStoryGateway;
import me.tintran.hackernews.data.HackerNewsApi;
import retrofit2.Call;

/**
 * Created by tin on 7/10/16.
 */
public class TopStoryProcessor implements Command {

  private StoryApiHelper storiesApiHelper;
  private StoryGateway storyGateway;
  private final TopStoryGateway topStoryGateway;
  private StoryCommentGateway storyCommentGateway;
  private Callback callback;
  private List<Integer> callList;

  public TopStoryProcessor(StoryApiHelper storiesApiHelper, StoryGateway storyGateway,
      TopStoryGateway topStoryGateway, StoryCommentGateway storyCommentGateway, Callback callback) {
    this.storiesApiHelper = storiesApiHelper;
    this.storyGateway = storyGateway;
    this.topStoryGateway = topStoryGateway;
    this.storyCommentGateway = storyCommentGateway;
    this.callback = callback;
  }

  @Override public void execute() {
    refreshTopStories();
  }

  @VisibleForTesting
  private void refreshTopStories() {
    int[] topStoryIds = storiesApiHelper.getTopStoryIds();
    if (topStoryIds == null) {
      callback.onComplete();
      return;
    }
    topStoryGateway.replaceTopStoryIds(topStoryIds);

    int[] updatedStories = storiesApiHelper.getUpdatedStoryIds();
    if (updatedStories == null){
      callback.onComplete();
      return;
    }
    final int[] localIdsFromDatabase = storyGateway.getLocalTopStoryIds();
    // Retrieve and insert the stories
    int[] idsToRetrieve = getIdsToRetrieve(topStoryIds, updatedStories, localIdsFromDatabase);
    callList = new ArrayList<>(idsToRetrieve.length);
    for (final int itemId : idsToRetrieve) {
      callList.add(itemId);
      TopStoriesCallback topStoriesCallback =
          new TopStoriesCallback(itemId, storyGateway, storyCommentGateway,
              new TopStoriesCallback.OnReturn() {
                @Override public void onReturn(Call<HackerNewsApi.StoryItem> call) {
                  callList.remove(Integer.valueOf(itemId));
                  stopServiceIfNeeded();
                }
              });
      storiesApiHelper.getStory(itemId, topStoriesCallback);
    }
  }

  int[] getIdsToRetrieve(int[] topStoryIds, int[] updatedIds, int[] storyIdsFromDatabase){
    return topStoryIds;
  }

  private void stopServiceIfNeeded() {
    if (callList.isEmpty()) {
      callback.onComplete();
    }
  }

  public interface Callback {
    void onComplete();
  }
}

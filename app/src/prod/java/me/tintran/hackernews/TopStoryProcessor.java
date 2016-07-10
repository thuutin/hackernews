package me.tintran.hackernews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.HackerNewsApi;
import retrofit2.Call;

/**
 * Created by tin on 7/10/16.
 */
public class TopStoryProcessor implements Command {

  private final HackerNewsApi.Stories storiesApi;
  private StoryGateway storyGateway;
  private final TopStoryGateway topStoryGateway;
  private StoryCommentGateway storyCommentGateway;
  private Callback callback;
  private List<Call> callList;

  public TopStoryProcessor(HackerNewsApi.Stories storiesApi, StoryGateway storyGateway,
      TopStoryGateway topStoryGateway, StoryCommentGateway storyCommentGateway, Callback callback) {
    this.storiesApi = storiesApi;
    this.storyGateway = storyGateway;
    this.topStoryGateway = topStoryGateway;
    this.storyCommentGateway = storyCommentGateway;
    this.callback = callback;
  }

  @Override public void execute() {
    refreshTopStories();
  }

  private void refreshTopStories() {
    Call<int[]> topStories = storiesApi.getTopStories();
    int[] topStoryIds = null;
    try {
      topStoryIds = topStories.execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (topStoryIds == null) {
      return;
    }

    topStoryGateway.replaceTopStoryIds(topStoryIds);

    Call<int[]> updatedStoriesCall = storiesApi.getUpdatedStories();
    int[] updatedStories = null;
    try {
      updatedStories = updatedStoriesCall.execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }
    final int[] localIdsFromDatabase = topStoryGateway.getLocalTopStoryIds();
    StoryHelper storyHelper = new StoryHelper(topStoryIds, updatedStories, localIdsFromDatabase);
    // Retrieve and insert the stories
    int[] idsToRetrieve = storyHelper.getIdsToRetrieve();
    callList = new ArrayList<>(idsToRetrieve.length);
    for (final int itemId : idsToRetrieve) {
      final Call<HackerNewsApi.StoryItem> storyItemCall = storiesApi.getStory(itemId);
      TopStoriesCallback topStoriesCallback =
          new TopStoriesCallback(itemId, storyGateway, storyCommentGateway,
              new TopStoriesCallback.OnReturn() {
                @Override public void onReturn(Call<HackerNewsApi.StoryItem> call) {
                  callList.remove(call);
                  stopServiceIfNeeded();
                }
              });
      callList.add(storyItemCall);
      storyItemCall.enqueue(topStoriesCallback);
    }
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

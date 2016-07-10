package me.tintran.hackernews;

import java.io.IOException;
import me.tintran.hackernews.data.HackerNewsApi;
import retrofit2.Call;

/**
 * Created by tin on 7/10/16.
 */
public interface StoryApiHelper {
  int[] getUpdatedStoryIds();

  int[] getTopStoryIds();

  void getStory(int itemId, TopStoriesCallback storiesCallback);

  class StoryApiHelperImpl implements StoryApiHelper {

    private HackerNewsApi.Stories storiesApi;

    public StoryApiHelperImpl(HackerNewsApi.Stories storiesApi) {
      this.storiesApi = storiesApi;
    }

    @Override
    public int[] getUpdatedStoryIds() {
      Call<int[]> updatedStoriesCall = storiesApi.getUpdatedStories();
      int[] updatedStories = null;
      try {
        updatedStories = updatedStoriesCall.execute().body();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return updatedStories;
    }

    @Override public int[] getTopStoryIds() {
      Call<int[]> topStories = storiesApi.getTopStories();
      int[] topStoryIds = null;
      try {
        topStoryIds = topStories.execute().body();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return topStoryIds;
    }

    @Override public void getStory(int itemId, TopStoriesCallback storiesCallback) {
      Call<HackerNewsApi.StoryItem> storyItemCall = storiesApi.getStory(itemId);
      storyItemCall.enqueue(storiesCallback);
    }
  }
}

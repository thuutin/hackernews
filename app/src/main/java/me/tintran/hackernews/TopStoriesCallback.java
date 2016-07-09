package me.tintran.hackernews;

import android.util.Log;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.HackerNewsApi.StoryItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by tin on 7/9/16.
 */

public class TopStoriesCallback implements Callback<StoryItem> {

  private int storyId;
  private StoryGateway storyGateway;
  private StoryCommentGateway storyCommentGateway;

  public TopStoriesCallback(int storyId, StoryGateway storyGateway, StoryCommentGateway storyCommentGateway) {
    this.storyId = storyId;
    this.storyGateway = storyGateway;
    this.storyCommentGateway = storyCommentGateway;
  }

  @Override public void onResponse(Call<StoryItem> call, Response<StoryItem> response) {
    StoryItem body = response.body();
    storyGateway.insertStory(body.id, body.title, body.descendants, body.score, body.time,
        body.type, body.url);
    storyCommentGateway.insert(body.id, body.kids);
  }

  @Override public void onFailure(Call<StoryItem> call, Throwable t) {
    Log.d("StoriesRepository",
        "Failure getting story id " + String.valueOf(storyId) + t.getMessage());

  }
}
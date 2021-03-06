package me.tintran.hackernews.sync;

import android.util.Log;
import me.tintran.hackernews.StoryCommentGateway;
import me.tintran.hackernews.StoryGateway;
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
  private OnReturn callOnReturn;

  public TopStoriesCallback(int storyId, StoryGateway storyGateway,
      StoryCommentGateway storyCommentGateway, OnReturn callOnReturn) {
    this.storyId = storyId;
    this.storyGateway = storyGateway;
    this.storyCommentGateway = storyCommentGateway;
    this.callOnReturn = callOnReturn;
  }

  @Override public void onResponse(Call<StoryItem> call, Response<StoryItem> response) {
    StoryItem body = response.body();
    storyGateway.insertStory(body.id, body.title, body.descendants, body.deleted, body.score, body.time,
        body.type, body.url, body.by);
    storyCommentGateway.insert(body.id, body.kids);
    callOnReturn.onReturn(call);
  }

  @Override public void onFailure(Call<StoryItem> call, Throwable t) {
    Log.d("StoriesRepository",
        "Failure getting story id " + String.valueOf(storyId) + t.getMessage());
    callOnReturn.onReturn(call);
  }

  interface OnReturn {
    void onReturn(Call<StoryItem> onReturn);
  }
}

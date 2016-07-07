package me.tintran.hackernews;

import android.support.v4.app.LoaderManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.TopStoriesUseCase;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tin on 7/6/16.
 */

public class StoriesRepository implements
    TopStoriesUseCase, CommentListUseCase {

  private LoaderManager loaderManager;
  private Callback callback;

  public StoriesRepository(LoaderManager loaderManager) {
    this.loaderManager = loaderManager;
  }

  @Override public void getTopStories(final Callback callback) {
    this.callback = callback;
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://hacker-news.firebaseio.com/v0/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    HackerNewsApi hackerNewsApi = retrofit.create(HackerNewsApi.class);
    Call<int[]> topStories = hackerNewsApi.getTopStories();
    topStories.enqueue(new retrofit2.Callback<int[]>() {
      @Override public void onResponse(Call<int[]> call, Response<int[]> response) {
        List<Story> stories = new ArrayList<>(response.body().length);
        for (int i = 0; i < response.body().length; i++) {
          final int itemId = response.body()[i];
          stories.add(new Story(itemId, String.valueOf(response.body()[i]), "Subtitle " + i));
        }
        callback.onComplete(stories);
      }

      @Override public void onFailure(Call<int[]> call, Throwable t) {
        Log.d("StoriesRepository" , "Failure " + t.getMessage());
      }
    });
  }


  @Override public void getCommentList(int storyId, final CommentListUseCase.Callback callback) {
  }

}

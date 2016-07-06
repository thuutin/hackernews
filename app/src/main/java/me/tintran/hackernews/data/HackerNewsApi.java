package me.tintran.hackernews.data;

import java.util.List;
import retrofit2.Call;

/**
 * Created by tin on 7/6/16.
 */

public interface HackerNewsApi {
  Call<List<TopStories>> getTopStories();

  class TopStories {
    int id;
  }
}

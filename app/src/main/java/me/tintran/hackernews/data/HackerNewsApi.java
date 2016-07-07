package me.tintran.hackernews.data;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by tin on 7/6/16.
 */

public interface HackerNewsApi {
  @GET(value = "topstories.json")
  Call<int[]> getTopStories();

}

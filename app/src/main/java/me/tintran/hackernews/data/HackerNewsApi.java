package me.tintran.hackernews.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by tin on 7/6/16.
 */

public interface HackerNewsApi {
  interface Stories {
    @GET(value = "topstories.json") Call<int[]> getTopStories();
    @GET(value = "item/{id}.json") Call<StoryItem> getStory(@Path("id") int storyId);
    //https://hacker-news.firebaseio.com/v0/updates/items.json
    @GET(value = "updates/items.json") Call<int[]> getUpdatedStories();
  }

  interface Comments {
    @GET(value = "item/{id}.json")
    Call<CommentItem> getComment(@Path("id") int commentId);
  }

  /**
   * Created by tin on 7/7/16.
   */
  final class StoryItem {
    public int id;
    public int descendants;
    public int score;
    public String by;
    public boolean deleted;
    public int[] kids;
    public String title;
    public long time;
    public String type;
    public String url;
  }

  final class CommentItem {
    //{
    //  "by": "eggy",
    //    "id": 12053570,
    //    "kids": [
    //  12053598,
    //      12053594
    //  ],
    //  "parent": 12053272,
    //    "text": "I was born and raised in Brooklyn, NY, but moved out to rural NJ in my thirties. I bought a house on a lake with no motorboats, plenty of black bears and raccoons and lots of trees. I now live in the rice fields of East Java, Indonesia, so I guess you can say I love the outdoors.<p>I do question the science or numbers in the study as much as I believe the basic premise to be true, however, correlation does not automatically imply cause. People suffering more after trees are removed can also mean that urbanization or development brought factories, or unhealthier air, rodents or any number of other negative factors with it.<p>I do intuitively relax more, and take great solace in my surroundings, and I do believe it is better for people. I would like to see more research on this; there have been a lot of debacles in the past two years in the social sciences and psychology with statistics and peer review. Some of the studies were taken for granted and are now under the microscope for being inconclusive or just wrong.<p>Yea for trees! And plants, animals and all that entails!",
    //    "time": 1467952179,
    //    "type": "comment"
    //}
    public int id;
    public String by;
    public int[] kids;
    public boolean deleted;
    public int parent;
    public String text;
    public long time;
    public String type;
  }
}

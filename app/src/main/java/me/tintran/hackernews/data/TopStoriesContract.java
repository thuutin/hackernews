package me.tintran.hackernews.data;

import android.provider.BaseColumns;

/**
 * Created by tin on 7/7/16.
 */

public class TopStoriesContract {

  public static abstract class StoryColumns implements BaseColumns {
    public static final String TABLE_NAME = "TopStories";
    public static final String STORYID = "storyId";
  }
}

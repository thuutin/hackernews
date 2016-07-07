package me.tintran.hackernews.data;

import android.provider.BaseColumns;

/**
 * Created by tin on 7/7/16.
 */

public class TopStoriesContract {

  static abstract class StoryColumns implements BaseColumns {
    static final String TABLE_NAME = "TopStories";
    static final String STORYID = "storyId";
  }
}

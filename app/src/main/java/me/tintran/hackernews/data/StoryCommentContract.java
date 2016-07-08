package me.tintran.hackernews.data;

import android.provider.BaseColumns;

/**
 * Created by tin on 7/8/16.
 */

public class StoryCommentContract {

  public static abstract class StoryCommentColumns implements BaseColumns {
    public static final String TABLE_NAME = "StoryComment";
    public static final String COLUMN_NAME_STORYID = "storyId";
    public static final String COLUMN_NAME_COMMENTID= "commentId";
  }
}

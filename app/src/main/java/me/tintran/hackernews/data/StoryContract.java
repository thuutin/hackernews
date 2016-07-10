package me.tintran.hackernews.data;

import android.provider.BaseColumns;

/**
 * Created by tin on 7/7/16.
 */

public class StoryContract {

  public StoryContract() {
  }

  public static abstract class StoryColumns implements BaseColumns {
    public static final String TABLE_NAME = "Story";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_DESCENDANTS = "descendants";
    public static final String COLUMN_NAME_SCORE = "score";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_BY = "by";
    public static final String COLUMN_NAME_URL = "url";
    public static final String COLUMN_NAME_DELETED = "deleted";
  }


}

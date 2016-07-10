package me.tintran.hackernews.data;

import android.provider.BaseColumns;

/**
 * Created by tin on 7/8/16.
 */

public class CommentContract {

  public static abstract class CommentColumns implements BaseColumns {
    public static final String TABLE_NAME = "Comment";
    public static final String COLUMN_NAME_TEXT = "text";
    public static final String COLUMN_NAME_BY = "by";
    public static final String COLUMN_NAME_PARENT = "parent";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_DELETED = "deleted";
  }
}

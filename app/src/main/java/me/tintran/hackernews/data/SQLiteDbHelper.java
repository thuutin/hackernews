package me.tintran.hackernews.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import me.tintran.hackernews.data.StoryCommentContract.StoryCommentColumns;

/**
 * Created by tin on 7/7/16.
 */

public class SQLiteDbHelper extends SQLiteOpenHelper {

  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INTEGER";
  private static final String COMMA_SEP = ",";

  private static final String SQL_CREATE_STORIES =
      "CREATE TABLE " + StoryContract.StoryColumns.TABLE_NAME + " (" +
          StoryContract.StoryColumns._ID + " INTEGER PRIMARY KEY," +
          StoryContract.StoryColumns.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
          StoryContract.StoryColumns.COLUMN_NAME_DESCENDANTS + INT_TYPE + COMMA_SEP +
          StoryContract.StoryColumns.COLUMN_NAME_SCORE + INT_TYPE + COMMA_SEP +
          StoryContract.StoryColumns.COLUMN_NAME_TIME + INT_TYPE + COMMA_SEP +
          StoryContract.StoryColumns.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
          StoryContract.StoryColumns.COLUMN_NAME_DELETED + INT_TYPE + COMMA_SEP +
          StoryContract.StoryColumns.COLUMN_NAME_URL + TEXT_TYPE +
          " )";

  private static final String SQL_CREATE_TOP_STORIES =
      "CREATE TABLE " + TopStoriesContract.StoryColumns.TABLE_NAME + " (" +
          TopStoriesContract.StoryColumns._ID + " INTEGER PRIMARY KEY," +
          TopStoriesContract.StoryColumns.STORYID + INT_TYPE +
          " )";

  private static final String SQL_CREATE_COMMENTS =
      "CREATE TABLE " + CommentContract.CommentColumns.TABLE_NAME + " (" +
          CommentContract.CommentColumns._ID + " INTEGER PRIMARY KEY," +
          CommentContract.CommentColumns.COLUMN_NAME_BY + TEXT_TYPE + COMMA_SEP +
          CommentContract.CommentColumns.COLUMN_NAME_PARENT + INT_TYPE + COMMA_SEP +
          CommentContract.CommentColumns.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
          CommentContract.CommentColumns.COLUMN_NAME_DELETED + INT_TYPE + COMMA_SEP +
          CommentContract.CommentColumns.COLUMN_NAME_TIME + INT_TYPE + COMMA_SEP +
          CommentContract.CommentColumns.COLUMN_NAME_TYPE + TEXT_TYPE +
          " )";

  private static final String SQL_CREATE_STORIES_COMMENTS =
      "CREATE TABLE " + StoryCommentColumns.TABLE_NAME + " (" +
          StoryCommentColumns._ID + " INTEGER PRIMARY KEY," +
          StoryCommentColumns.COLUMN_NAME_STORYID + INT_TYPE + COMMA_SEP +
          StoryCommentColumns.COLUMN_NAME_COMMENTID + INT_TYPE + COMMA_SEP +
          " UNIQUE (" + StoryCommentColumns.COLUMN_NAME_COMMENTID + COMMA_SEP +
          StoryCommentColumns.COLUMN_NAME_STORYID + ") " +
          "ON CONFLICT REPLACE" +
          " )";

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "HackerNews.db";

  public SQLiteDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_STORIES);
    db.execSQL(SQL_CREATE_TOP_STORIES);
    db.execSQL(SQL_CREATE_COMMENTS);
    db.execSQL(SQL_CREATE_STORIES_COMMENTS);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}

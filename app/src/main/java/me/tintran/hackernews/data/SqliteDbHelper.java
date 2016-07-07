package me.tintran.hackernews.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tin on 7/7/16.
 */

public class SqliteDbHelper extends SQLiteOpenHelper {

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
          StoryContract.StoryColumns.COLUMN_NAME_URL + TEXT_TYPE +
      " )";

  private static final String SQL_CREATE_TOP_STORIES =
      "CREATE TABLE " + TopStoriesContract.StoryColumns.TABLE_NAME + " (" +
          TopStoriesContract.StoryColumns._ID + " INTEGER PRIMARY KEY," +
          TopStoriesContract.StoryColumns.STORYID + INT_TYPE +
          " )";

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "HackerNews.db";

  public SqliteDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_STORIES);
    db.execSQL(SQL_CREATE_TOP_STORIES);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}

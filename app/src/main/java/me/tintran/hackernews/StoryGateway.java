package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import me.tintran.hackernews.data.StoryContract;

/**
 * Created by tin on 7/9/16.
 */

interface StoryGateway {
  void insertStory(int id, String title, int descendants, int score, long time, String type, String url);

  class SqliteStoryGateway implements StoryGateway {
    private SQLiteDatabase sqLiteDatabase;

    public SqliteStoryGateway(SQLiteDatabase sqLiteDatabase) {
      this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void insertStory(int id, String title, int descendants, int score, long time, String type,
        String url) {
      final ContentValues contentvalues = new ContentValues();
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TITLE, title);
      contentvalues.put(StoryContract.StoryColumns._ID, id);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_DESCENDANTS, descendants);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_SCORE, score);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TIME, time);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TYPE, type);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_URL, url);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        sqLiteDatabase.beginTransactionNonExclusive();
      } else {
        sqLiteDatabase.beginTransaction();
      }
      Log.d("StoryGateway", "In Transaction " + id + " thread " + Thread.currentThread().getId());
      try {
        sqLiteDatabase.insertWithOnConflict(StoryContract.StoryColumns.TABLE_NAME, null, contentvalues,
            SQLiteDatabase.CONFLICT_REPLACE);
        sqLiteDatabase.setTransactionSuccessful();
      } finally {
        sqLiteDatabase.endTransaction();
        Log.d("StoryGateway", "Out Transaction " + id + " thread " + Thread.currentThread().getId());

      }

    }
  }
}

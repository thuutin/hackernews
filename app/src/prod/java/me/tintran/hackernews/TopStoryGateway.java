package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import me.tintran.hackernews.data.TopStoriesContract;

/**
 * Created by tin on 7/9/16.
 */
public interface TopStoryGateway {
  void replace(int[] topstoryids);

  public class SqliteTopStoryGateway implements TopStoryGateway {

    private SQLiteDatabase sqLiteDatabase;

    public SqliteTopStoryGateway(SQLiteDatabase sqLiteDatabase) {
      this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override public void replace(int[] topstoryids) {
      // Replacing all records in the TopStories table
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        sqLiteDatabase.beginTransactionNonExclusive();
      } else {
        sqLiteDatabase.beginTransaction();
      }
      try {
        sqLiteDatabase.delete(TopStoriesContract.StoryColumns.TABLE_NAME, null, null);
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < topstoryids.length; i++) {
          contentValues.clear();
          contentValues.put(TopStoriesContract.StoryColumns.STORYID, topstoryids[i]);
          sqLiteDatabase.insert(TopStoriesContract.StoryColumns.TABLE_NAME, null,
              contentValues);
        }
        sqLiteDatabase.setTransactionSuccessful();
      } finally {
        sqLiteDatabase.endTransaction();
      }
    }
  }
}

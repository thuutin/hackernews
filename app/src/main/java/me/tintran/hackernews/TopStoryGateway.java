package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.text.Html;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.StoryContract;
import me.tintran.hackernews.data.TopStoriesContract;

/**
 * Created by tin on 7/9/16.
 */
interface TopStoryGateway {
  @WorkerThread void replaceTopStoryIds(int[] topstoryids);

  class SQLiteTopStoryGateway implements TopStoryGateway {

    private SQLiteDatabase sqLiteDatabase;

    SQLiteTopStoryGateway(SQLiteDatabase sqLiteDatabase) {
      this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override public void replaceTopStoryIds(int[] topstoryids) {
      // Replacing all records in the TopStories table
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        sqLiteDatabase.beginTransactionNonExclusive();
      } else {
        sqLiteDatabase.beginTransaction();
      }
      try {
        sqLiteDatabase.delete(TopStoriesContract.StoryColumns.TABLE_NAME, null, null);
        ContentValues contentValues = new ContentValues();
        for (int topStoryId : topstoryids) {
          contentValues.clear();
          contentValues.put(TopStoriesContract.StoryColumns.STORYID, topStoryId);
          sqLiteDatabase.insert(TopStoriesContract.StoryColumns.TABLE_NAME, null, contentValues);
        }
        sqLiteDatabase.setTransactionSuccessful();
      } finally {
        sqLiteDatabase.endTransaction();
      }
    }
  }
}

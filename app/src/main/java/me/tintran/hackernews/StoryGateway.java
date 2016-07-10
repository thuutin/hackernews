package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.StoryContract;
import me.tintran.hackernews.data.TopStoriesContract;

/**
 * Created by tin on 7/9/16.
 */

interface StoryGateway {

  void insertStory(int id, String title, int descendants, boolean deleted, int score, long time,
      String type, String url);

  @WorkerThread List<Story> getTopStories();

  @WorkerThread int[] getLocalTopStoryIds();

  class SqliteStoryGateway implements StoryGateway {
    private SQLiteDatabase sqLiteDatabase;

    public SqliteStoryGateway(SQLiteDatabase sqLiteDatabase) {
      this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void insertStory(int id, String title, int descendants, boolean deleted, int score,
        long time, String type, String url) {
      final ContentValues contentvalues = new ContentValues();
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TITLE, title);
      contentvalues.put(StoryContract.StoryColumns._ID, id);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_DESCENDANTS, descendants);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_SCORE, score);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_DELETED, deleted);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TIME, time);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TYPE, type);
      contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_URL, url);
      sqLiteDatabase.insertWithOnConflict(StoryContract.StoryColumns.TABLE_NAME, null,
          contentvalues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @WorkerThread @Override public List<Story> getTopStories() {
      String tableName = TopStoriesContract.StoryColumns.TABLE_NAME
          + " LEFT JOIN "
          + StoryContract.StoryColumns.TABLE_NAME
          + " ON "
          + StoryContract.StoryColumns.TABLE_NAME
          + "."
          + StoryContract.StoryColumns._ID
          + " = "
          + TopStoriesContract.StoryColumns.STORYID;

      Cursor query = sqLiteDatabase.query(tableName, null, null, null, null, null,
          TopStoriesContract.StoryColumns.TABLE_NAME
              + "."
              + TopStoriesContract.StoryColumns._ID
              + " ASC");

      int size = query.getCount();
      List<Story> results = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        query.moveToPosition(i);
        Story story =
            new Story(query.getInt(query.getColumnIndex(TopStoriesContract.StoryColumns.STORYID)),
                query.getString(query.getColumnIndex(StoryContract.StoryColumns.COLUMN_NAME_TITLE)),
                query.getString(query.getColumnIndex(TopStoriesContract.StoryColumns.STORYID)));
        results.add(story);
      }
      query.close();
      sqLiteDatabase.close();
      return results;
    }

    @WorkerThread @Override public int[] getLocalTopStoryIds() {
      Cursor storiesIdsFromDatabase = sqLiteDatabase.query(StoryContract.StoryColumns.TABLE_NAME,
          new String[] { StoryContract.StoryColumns._ID }, null, null, null, null, null);
      int count = storiesIdsFromDatabase.getCount();
      int[] ids = new int[count];
      for (int i = 0; i < count; i++) {
        storiesIdsFromDatabase.moveToPosition(i);
        int idColumnIndex = storiesIdsFromDatabase.getColumnIndex(StoryContract.StoryColumns._ID);
        ids[i] = storiesIdsFromDatabase.getInt(idColumnIndex);
      }
      storiesIdsFromDatabase.close();
      return ids;
    }
  }
}

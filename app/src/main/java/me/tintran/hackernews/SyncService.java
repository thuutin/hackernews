package me.tintran.hackernews;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.SqliteDbHelper;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.StoryContract;
import me.tintran.hackernews.data.StoryItem;
import me.tintran.hackernews.data.TopStoriesContract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tin on 7/7/16.
 */

public class SyncService extends IntentService {

  private SQLiteDatabase writableDatabase;
  private Handler handler;

  public SyncService() {
    super(SyncService.class.getSimpleName());
  }

  @Override public void onCreate() {
    super.onCreate();
    SqliteDbHelper sqliteDbHelper = new SqliteDbHelper(this);
    writableDatabase = sqliteDbHelper.getWritableDatabase();
    handler = new Handler();
  }

  @Override protected void onHandleIntent(Intent intent) {

    GsonConverterFactory factory = GsonConverterFactory.create();
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://hacker-news.firebaseio.com/v0/")
        .callbackExecutor(new Executor() {
          @Override public void execute(@NonNull Runnable command) {
            handler.post(command);
          }
        })
        .addConverterFactory(factory)
        .build();
    HackerNewsApi hackerNewsApi = retrofit.create(HackerNewsApi.class);
    Call<int[]> topStories = hackerNewsApi.getTopStories();
    int[] body = null;
    try {
      body = topStories.execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (body == null) {
      return;
    }

    // Replacing all records in the TopStories table
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      writableDatabase.beginTransactionNonExclusive();
    } else {
      writableDatabase.beginTransaction();
    }
    try {
      writableDatabase.delete(TopStoriesContract.StoryColumns.TABLE_NAME, null, null);
      ContentValues contentValues = new ContentValues();
      for (int i = 0; i < body.length; i++) {
        contentValues.clear();
        contentValues.put(TopStoriesContract.StoryColumns.STORYID, body[i]);
        writableDatabase.insert(TopStoriesContract.StoryColumns.TABLE_NAME, null, contentValues);
      }
      writableDatabase.setTransactionSuccessful();
    } finally {
      writableDatabase.endTransaction();
    }

    final List<Call<StoryItem>> callList = new LinkedList<>();
    // Retrieve and insert the stories
    for (final int itemId : body) {
      Cursor itemInDatabase = writableDatabase.query(StoryContract.StoryColumns.TABLE_NAME,
          new String[] { StoryContract.StoryColumns._ID }, StoryContract.StoryColumns._ID + " = ?",
          new String[] { String.valueOf(itemId) }, null, null, null, String.valueOf(1));
      if (itemInDatabase.getCount() != 0) {
        itemInDatabase.close();
        continue;
      }
      itemInDatabase.close();
      Call<StoryItem> storyItemCall = hackerNewsApi.getStory(itemId);
      callList.add(storyItemCall);
      storyItemCall.enqueue(new Callback<StoryItem>() {
        @Override public void onResponse(Call<StoryItem> call, Response<StoryItem> response) {
          StoryItem body = response.body();
          ContentValues contentvalues = new ContentValues();
          contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TITLE, body.title);
          contentvalues.put(StoryContract.StoryColumns._ID, body.id);
          contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_DESCENDANTS, body.descendants);
          contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_SCORE, body.score);
          contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TIME, body.time);
          contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_TYPE, body.type);
          contentvalues.put(StoryContract.StoryColumns.COLUMN_NAME_URL, body.url);
          writableDatabase.insertWithOnConflict(StoryContract.StoryColumns.TABLE_NAME, null,
              contentvalues, SQLiteDatabase.CONFLICT_REPLACE);
          callList.remove(call);
        }

        @Override public void onFailure(Call<StoryItem> call, Throwable t) {
          Log.d("StoriesRepository",
              "Failure getting story id " + String.valueOf(itemId) + t.getMessage());
          callList.remove(call);
        }
      });
    }

    while (!callList.isEmpty()) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Log.d(SyncService.class.getSimpleName(), "Hello + I am done");
  }

  @Override public void onDestroy() {
    if (writableDatabase != null && writableDatabase.isOpen()) {
      writableDatabase.close();
    }
    super.onDestroy();
  }
}

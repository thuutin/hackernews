package me.tintran.hackernews;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.HackerNewsApi.StoryItem;
import me.tintran.hackernews.data.SqliteDbHelper;
import me.tintran.hackernews.data.StoryCommentContract;
import me.tintran.hackernews.data.StoryCommentContract.StoryCommentColumns;
import me.tintran.hackernews.data.StoryContract;
import me.tintran.hackernews.data.StoryContract.StoryColumns;
import me.tintran.hackernews.data.TopStoriesContract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tin on 7/7/16.
 */

public class SyncService extends Service {

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private static final String baseUrl = "https://hacker-news.firebaseio.com/v0/";

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    GsonConverterFactory factory = GsonConverterFactory.create();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
        .callbackExecutor(executorService)
        .addConverterFactory(factory)
        .build();
    final HackerNewsApi hackerNewsApi = retrofit.create(HackerNewsApi.class);
    executorService.submit(new Runnable() {
      @Override public void run() {
        SqliteDbHelper sqliteDbHelper = new SqliteDbHelper(SyncService.this);
        final SQLiteDatabase writableDatabase = sqliteDbHelper.getWritableDatabase();
        Call<int[]> topStories = hackerNewsApi.getTopStories();
        int[] body = null;
        try {
          body = topStories.execute().body();
        } catch (IOException e) {
          e.printStackTrace();
        }

        if (body != null) {
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
              writableDatabase.insert(TopStoriesContract.StoryColumns.TABLE_NAME, null,
                  contentValues);
            }
            writableDatabase.setTransactionSuccessful();
          } finally {
            writableDatabase.endTransaction();
          }

          final List<Call<StoryItem>> callList = new LinkedList<>();
          // Retrieve and insert the stories
          for (final int itemId : body) {
            //Cursor itemInDatabase = writableDatabase.query(StoryContract.StoryColumns.TABLE_NAME,
            //    new String[] { StoryContract.StoryColumns._ID }, StoryContract.StoryColumns._ID + " = ?",
            //    new String[] { String.valueOf(itemId) }, null, null, null, String.valueOf(1));
            //if (itemInDatabase.getCount() != 0) {
            //  itemInDatabase.close();
            //  continue;
            //}
            //itemInDatabase.close();
            Call<StoryItem> storyItemCall = hackerNewsApi.getStory(itemId);
            callList.add(storyItemCall);
            storyItemCall.enqueue(new Callback<StoryItem>() {
              @Override public void onResponse(Call<StoryItem> call, Response<StoryItem> response) {
                StoryItem body = response.body();
                final ContentValues contentvalues = new ContentValues();
                contentvalues.put(StoryColumns.COLUMN_NAME_TITLE, body.title);
                contentvalues.put(StoryColumns._ID, body.id);
                contentvalues.put(StoryColumns.COLUMN_NAME_DESCENDANTS, body.descendants);
                contentvalues.put(StoryColumns.COLUMN_NAME_SCORE, body.score);
                contentvalues.put(StoryColumns.COLUMN_NAME_TIME, body.time);
                contentvalues.put(StoryColumns.COLUMN_NAME_TYPE, body.type);
                contentvalues.put(StoryColumns.COLUMN_NAME_URL, body.url);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                  writableDatabase.beginTransactionNonExclusive();
                } else {
                  writableDatabase.beginTransaction();
                }
                try {
                  writableDatabase.insertWithOnConflict(StoryColumns.TABLE_NAME, null,
                      contentvalues, SQLiteDatabase.CONFLICT_REPLACE);
                  if (body.kids == null) {
                    callList.remove(call);
                    return;
                  }
                  contentvalues.clear();
                  contentvalues.put(StoryCommentColumns.COLUMN_NAME_STORYID, body.id);
                  for (int i = 0; i < body.kids.length; i++) {
                    contentvalues.put(StoryCommentColumns.COLUMN_NAME_COMMENTID, body.kids[i]);
                    writableDatabase.insertWithOnConflict(StoryCommentColumns.TABLE_NAME, null,
                        contentvalues, SQLiteDatabase.CONFLICT_REPLACE);
                  }
                  writableDatabase.setTransactionSuccessful();
                } finally {
                  writableDatabase.endTransaction();
                }

                callList.remove(call);
                stopServiceIfNeeded(callList, writableDatabase);
              }

              @Override public void onFailure(Call<StoryItem> call, Throwable t) {
                Log.d("StoriesRepository",
                    "Failure getting story id " + String.valueOf(itemId) + t.getMessage());
                callList.remove(call);
                stopServiceIfNeeded(callList, writableDatabase);
              }
            });
          }
          Log.d(SyncService.class.getSimpleName(), "Hello + I am done");
        }
      }
    });
    return START_NOT_STICKY;
  }

  private void stopServiceIfNeeded(List callList, SQLiteDatabase sqLiteDatabase) {
    if (callList.isEmpty()) {
      stopSelf();
      if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
        sqLiteDatabase.close();
      }
    }
  }

  @Override public void onDestroy() {

    super.onDestroy();
  }
}

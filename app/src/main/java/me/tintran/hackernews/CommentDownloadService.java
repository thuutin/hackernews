package me.tintran.hackernews;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.tintran.hackernews.data.CommentContract;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.HackerNewsApi.CommentItem;
import me.tintran.hackernews.data.SqliteDbHelper;
import me.tintran.hackernews.data.StoryCommentContract;
import me.tintran.hackernews.data.StoryCommentContract.StoryCommentColumns;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tin on 7/8/16.
 */

public class CommentDownloadService extends Service {
  private static final String baseUrl = "https://hacker-news.firebaseio.com/v0/";

  public static final String STORY_ID = "storyId";
  private ExecutorService executorService;

  public CommentDownloadService() {
    super();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    executorService = Executors.newSingleThreadExecutor();
  }

  @Override public int onStartCommand(final Intent intent, int flags, int startId) {
    final int storyId = intent.getIntExtra(STORY_ID, -1);
    if (storyId == -1) {
      throw new IllegalStateException("Can not handle this intent");
    }

    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
        .callbackExecutor(executorService)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    final HackerNewsApi hackerNewsApi = retrofit.create(HackerNewsApi.class);

    executorService.submit(new Runnable() {
      @Override public void run() {
        @SuppressWarnings("SpellCheckingInspection") SqliteDbHelper sqliteDbHelper =
            new SqliteDbHelper(CommentDownloadService.this);
        final SQLiteDatabase writableDatabase = sqliteDbHelper.getWritableDatabase();
        Cursor query = writableDatabase.query(StoryCommentColumns.TABLE_NAME, null,
            StoryCommentColumns.COLUMN_NAME_STORYID + " = ? ",
            new String[] { String.valueOf(storyId) }, null, null, null);

        int count = query.getCount();
        Log.d("CommentDownloadService", "loading comments " + count + " items");
        final List<Call<CommentItem>> callList = new ArrayList<>(count);
        for (int i = 0; i < query.getCount(); i++) {
          query.moveToPosition(i);
          final int commentId =
              query.getInt(query.getColumnIndex(StoryCommentColumns.COLUMN_NAME_COMMENTID));
          final Call<CommentItem> comment = hackerNewsApi.getComment(commentId);

          callList.add(comment);
          comment.enqueue(new Callback<CommentItem>() {
            @Override
            public void onResponse(Call<CommentItem> call, Response<CommentItem> response) {
              CommentItem commentItem = response.body();
              ContentValues contentValues = new ContentValues();
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_BY, commentItem.by);
              contentValues.put(CommentContract.CommentColumns._ID, commentItem.id);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_PARENT,
                  commentItem.parent);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TEXT, commentItem.text);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TIME, commentItem.time);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TYPE, commentItem.type);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TYPE, commentItem.type);

              writableDatabase.insertWithOnConflict(CommentContract.CommentColumns.TABLE_NAME, null,
                  contentValues, SQLiteDatabase.CONFLICT_REPLACE);
              callList.remove(call);
              Log.d("CommentDownloadService", "Done loading comment" + commentId);
              stopServiceIfNeeded(callList, writableDatabase);
            }

            @Override public void onFailure(Call<CommentItem> call, Throwable t) {
              callList.remove(call);
              Log.d("CommentDownloadService", "failed loading " + commentId);
              stopServiceIfNeeded(callList, writableDatabase);
            }
          });
        }

        query.close();
      }
    });

    return START_NOT_STICKY;
  }

  @Override public void onDestroy() {
    //executorService.shutdown();
     // TODO Find a way to shutdown this service
    super.onDestroy();
  }

  private void stopServiceIfNeeded(List<Call<CommentItem>> callList,
      SQLiteDatabase sqLiteDatabase) {
    if (callList.isEmpty()) {
      Log.d("CommentDownloadService", "DONE");
      stopSelf();
      if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
        sqLiteDatabase.close();
      }
    }
  }
}

package me.tintran.hackernews;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.Executor;
import me.tintran.hackernews.StoryCommentGateway.SQLiteStoryCommentGateway;
import me.tintran.hackernews.StoryGateway.SqliteStoryGateway;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.HackerNewsApi.StoryItem;
import me.tintran.hackernews.data.SQLiteDbHelper;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tin on 7/7/16.
 */

public class SyncService extends Service {

  private Handler serviceHandler;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private static final String baseUrl = "https://hacker-news.firebaseio.com/v0/";
  private SQLiteDatabase sqLiteDatabase;

  @Override public void onCreate() {
    super.onCreate();
    HandlerThread handlerThread =
        new HandlerThread(SyncService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    Looper looper = handlerThread.getLooper();
    serviceHandler = new Handler(looper);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    final HackerNewsApi hackerNewsApi = getHackerNewsApi();
    serviceHandler.post(new Runnable() {
      @Override public void run() {
        SQLiteDbHelper SQLiteDbHelper = new SQLiteDbHelper(SyncService.this);
        sqLiteDatabase = SQLiteDbHelper.getWritableDatabase();
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
        TopStoryGateway topStoryGateway = new TopStoryGateway.SQLiteTopStoryGateway(sqLiteDatabase);
        topStoryGateway.replace(body);

        // Retrieve and insert the stories
        for (final int itemId : body) {
          Call<StoryItem> storyItemCall = hackerNewsApi.getStory(itemId);
          final StoryGateway storyGateway = new SqliteStoryGateway(sqLiteDatabase);
          SQLiteStoryCommentGateway storyCommentGateway =
              new SQLiteStoryCommentGateway(sqLiteDatabase);
          storyItemCall.enqueue(new TopStoriesCallback(itemId, storyGateway, storyCommentGateway));
        }
      }
    });
    return START_NOT_STICKY;
  }

  //private void shutdownServiceIfNeeded() {
  //  if (callList.isEmpty()) {
  //    stopSelf();
  //  }
  //}

  private HackerNewsApi getHackerNewsApi() {
    GsonConverterFactory factory = GsonConverterFactory.create();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).callbackExecutor(new Executor() {
      @Override public void execute(@NonNull Runnable command) {
        serviceHandler.post(command);
      }
    }).addConverterFactory(factory).build();
    return retrofit.create(HackerNewsApi.class);
  }

  @Override public void onDestroy() {
    if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
      sqLiteDatabase.close();
    }
    super.onDestroy();
    Log.d(SyncService.class.getSimpleName(), "Hello + I am done");
  }
}

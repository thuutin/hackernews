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
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.tintran.hackernews.StoryGateway.SqliteStoryGateway;
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

  private ServiceHandler serviceHandler;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private static final String baseUrl = "https://hacker-news.firebaseio.com/v0/";
  public SQLiteDatabase sqLiteDatabase;
  final List<Call<StoryItem>> callList = new LinkedList<>();

  @Override public void onCreate() {
    super.onCreate();
    HandlerThread handlerThread =
        new HandlerThread(SyncService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    Looper looper = handlerThread.getLooper();
    serviceHandler = new ServiceHandler(looper);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    final HackerNewsApi hackerNewsApi = getHackerNewsApi();
    serviceHandler.post(new Runnable() {
      @Override public void run() {
        SqliteDbHelper sqliteDbHelper = new SqliteDbHelper(SyncService.this);
        sqLiteDatabase = sqliteDbHelper.getWritableDatabase();
        Call<int[]> topStories = hackerNewsApi.getTopStories();
        int[] body = null;
        try {
          body = topStories.execute().body();
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (body != null) {

          TopStoryGateway topStoryGateway =
              new TopStoryGateway.SqliteTopStoryGateway(sqLiteDatabase);
          topStoryGateway.replace(body);

          // Retrieve and insert the stories
          for (final int itemId : body) {
            Call<StoryItem> storyItemCall = hackerNewsApi.getStory(itemId);
            callList.add(storyItemCall);
            final StoryGateway storyGateway = new SqliteStoryGateway(sqLiteDatabase);
            storyItemCall.enqueue(new Callback<StoryItem>() {
              @Override public void onResponse(Call<StoryItem> call, Response<StoryItem> response) {
                StoryItem body = response.body();
                storyGateway.insertStory(body.id, body.title, body.descendants, body.score,
                    body.time, body.type, body.url);

                StoryCommentGateway storyCommentGateway =
                    new StoryCommentGateway.SqliteStoryCommentGateway(sqLiteDatabase);
                storyCommentGateway.insert(body.id, body.kids);
                callList.remove(call);
              }

              @Override public void onFailure(Call<StoryItem> call, Throwable t) {
                Log.d("StoriesRepository",
                    "Failure getting story id " + String.valueOf(itemId) + t.getMessage());
                callList.remove(call);
              }
            });
          }
        }
      }
    });
    return START_NOT_STICKY;
  }

  private HackerNewsApi getHackerNewsApi() {
    GsonConverterFactory factory = GsonConverterFactory.create();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).callbackExecutor(new Executor() {
      @Override public void execute(@NonNull Runnable command) {
        Message message = serviceHandler.obtainMessage();
        message.obj = command;
        serviceHandler.sendMessage(message);
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

  private class ServiceHandler extends Handler {

    ServiceHandler(Looper looper) {
      super(looper);
    }

    @Override public void handleMessage(Message msg) {
      ((Runnable) msg.obj).run();
      if (callList.isEmpty()) {
        stopSelf();
      }
    }
  }
}

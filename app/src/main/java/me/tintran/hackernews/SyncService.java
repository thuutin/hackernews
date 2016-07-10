package me.tintran.hackernews;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.concurrent.Executor;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.SQLiteDbHelper;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tin on 7/7/16.
 */

public class SyncService extends Service {
  public static final String UPDATE_TOP_STORIES = "UPDATE_TOP_STORIES";
  public static final String DOWNLOAD_COMMENT_FOR_STORY = "DOWNLOAD_COMMENT_FOR_STORY";

  public static final String STORY_ID = "storyId";
  public static final String DONE_DOWNLOAD_COMMENTS = "DONE_DOWNLOAD_COMMENTS";
  public static final String DONE_DOWNLOAD_STORIES = "DONE_DOWNLOAD_STORIES";

  private Handler serviceHandler;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private static final String baseUrl = "https://hacker-news.firebaseio.com/v0/";

  @Override public void onCreate() {
    super.onCreate();
    HandlerThread handlerThread =
        new HandlerThread(SyncService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    Log.d(SyncService.class.getSimpleName(),
        "Start Handler Thread " + handlerThread.getId() + "  " + toString());
    Looper looper = handlerThread.getLooper();
    SQLiteDbHelper sqliteDbHelper = new SQLiteDbHelper(SyncService.this);
    serviceHandler =
        new ServiceHandler(looper, sqliteDbHelper, getHackerNewsApi(HackerNewsApi.Stories.class),
            getHackerNewsApi(HackerNewsApi.Comments.class), new StopListener() {
          @Override public void notifyStop() {
            stopSelf();
          }

          @Override public void notifyLoadingCommentComplete(int storyId) {
            Intent intent = new Intent();
            intent.setAction(DONE_DOWNLOAD_COMMENTS);
            intent.setType("comment/" + storyId);
            LocalBroadcastManager.getInstance(SyncService.this).sendBroadcast(intent);
          }

          @Override public void notifyLoadingTopStoryComplete() {
            Intent intent = new Intent();
            intent.setAction(DONE_DOWNLOAD_STORIES);
            LocalBroadcastManager.getInstance(SyncService.this).sendBroadcast(intent);
          }
        });
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    String action = intent.getAction();

    switch (action) {
      case UPDATE_TOP_STORIES: {
        if (!serviceHandler.hasMessages(ServiceHandler.DOWNLOAD_TOP_STORIES)) {
          Message message = serviceHandler.obtainMessage();
          message.what = ServiceHandler.DOWNLOAD_TOP_STORIES;
          serviceHandler.sendMessage(message);
        }
        break;
      }

      case DOWNLOAD_COMMENT_FOR_STORY: {
        final int storyId = intent.getIntExtra(STORY_ID, -1);
        if (storyId == -1) {
          throw new IllegalStateException("Can not handle this intent");
        }
        Message message = serviceHandler.obtainMessage();
        message.what = ServiceHandler.DOWNLOAD_COMMENT_FOR_STORY;
        message.obj = storyId;
        serviceHandler.sendMessage(message);
        break;
      }
      default:
        throw new UnsupportedOperationException();
    }

    return START_NOT_STICKY;
  }

  private <T> T getHackerNewsApi(Class<T> tClass) {
    GsonConverterFactory factory = GsonConverterFactory.create();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).callbackExecutor(new Executor() {
      @Override public void execute(@NonNull Runnable command) {
        serviceHandler.post(command);
      }
    }).addConverterFactory(factory).build();
    return retrofit.create(tClass);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Log.d(SyncService.class.getSimpleName(), "Hello + I am done");
  }

}

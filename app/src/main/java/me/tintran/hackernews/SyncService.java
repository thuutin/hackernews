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
    Log.d(SyncService.class.getSimpleName(), "Start Handler Thread " + handlerThread.getId() + "  " + toString());
    Looper looper = handlerThread.getLooper();
    SQLiteDbHelper sqliteDbHelper = new SQLiteDbHelper(SyncService.this);
    serviceHandler = new ServiceHandler(looper, sqliteDbHelper, getHackerNewsApi(), new StopListener() {
      @Override public void notifyStop() {
        stopSelf();
      }
    });
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Message message = serviceHandler.obtainMessage();
    message.what = ServiceHandler.DOWNLOAD_TOP_STORIES;
    serviceHandler.sendMessage(message);
    return START_NOT_STICKY;
  }

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
    super.onDestroy();
    Log.d(SyncService.class.getSimpleName(), "Hello + I am done");
  }
}

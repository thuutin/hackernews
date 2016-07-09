package me.tintran.hackernews;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.HackerNewsApi;
import retrofit2.Call;

/**
 * Created by tin on 7/9/16.
 */

public final class ServiceHandler extends Handler {

  static final int DOWNLOAD_TOP_STORIES = 10001;

  private SQLiteDatabase sqLiteDatabase;
  private final SQLiteOpenHelper sqLiteOpenHelper;
  private HackerNewsApi hackerNewsApi;
  private final StopListener stopListener;
  private List<Call> callList = new ArrayList<>();

  ServiceHandler(Looper looper, SQLiteOpenHelper sqLiteOpenHelper, HackerNewsApi hackerNewsApi, StopListener stopListener) {
    super(looper);
    this.sqLiteOpenHelper = sqLiteOpenHelper;
    this.hackerNewsApi = hackerNewsApi;
    this.stopListener = stopListener;
  }

  @Override public void handleMessage(Message msg) {
    initDatabaseIfNeeded();
    switch (msg.what) {
      case DOWNLOAD_TOP_STORIES:
        Call<int[]> topStories = hackerNewsApi.getTopStories();
        int[] topStoryIds = null;
        try {
          topStoryIds = topStories.execute().body();
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (topStoryIds == null) {
          return;
        }

        TopStoryGateway topStoryGateway =
            new TopStoryGateway.SQLiteTopStoryGateway(sqLiteDatabase);
        topStoryGateway.replaceTopStoryIds(topStoryIds);

        // Retrieve and insert the stories
        for (final int itemId : topStoryIds) {
          final Call<HackerNewsApi.StoryItem> storyItemCall = hackerNewsApi.getStory(itemId);
          final StoryGateway storyGateway = new StoryGateway.SqliteStoryGateway(sqLiteDatabase);
          StoryCommentGateway.SQLiteStoryCommentGateway storyCommentGateway =
              new StoryCommentGateway.SQLiteStoryCommentGateway(sqLiteDatabase);
          TopStoriesCallback callback =
              new TopStoriesCallback(itemId, storyGateway, storyCommentGateway,
                  new TopStoriesCallback.OnReturn<Call<HackerNewsApi.StoryItem>>() {
                    @Override public void onReturn(Call<HackerNewsApi.StoryItem> call) {
                      callList.add(call);
                      stopServiceIfNeeded();
                    }
                  });
          storyItemCall.enqueue(callback);
        }
    }
  }

  private void stopServiceIfNeeded() {
    if (callList.isEmpty()){
      if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
        sqLiteDatabase.close();
      }
      stopListener.notifyStop();
    }
  }

  @WorkerThread
  private void initDatabaseIfNeeded() {
    if (sqLiteDatabase == null){
      sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }
  }
}

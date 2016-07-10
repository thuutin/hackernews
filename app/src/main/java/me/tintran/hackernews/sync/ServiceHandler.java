package me.tintran.hackernews.sync;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.util.SparseArray;
import me.tintran.hackernews.StoryCommentGateway;
import me.tintran.hackernews.StoryCommentGateway.SQLiteStoryCommentGateway;
import me.tintran.hackernews.StoryGateway;
import me.tintran.hackernews.TopStoryGateway;
import me.tintran.hackernews.data.CommentGateway;
import me.tintran.hackernews.data.HackerNewsApi;

/**
 * Created by tin on 7/9/16.
 */

public final class ServiceHandler extends Handler {

  static final int DOWNLOAD_TOP_STORIES = 10001;
  static final int DOWNLOAD_COMMENT_FOR_STORY = 10002;

  private SQLiteDatabase sqLiteDatabase;
  private final SQLiteOpenHelper sqLiteOpenHelper;
  private HackerNewsApi.Stories storiesApi;
  private HackerNewsApi.Comments commentsApi;
  private final ServiceHandlerInteraction serviceHandlerInteraction;

  private SparseArray<Command> commentCommands = new SparseArray<>();
  private Command topStoryProcessor;

  ServiceHandler(Looper looper, SQLiteOpenHelper sqLiteOpenHelper, HackerNewsApi.Stories storiesApi,
      HackerNewsApi.Comments commentsApi, ServiceHandlerInteraction serviceHandlerInteraction) {
    super(looper);
    this.sqLiteOpenHelper = sqLiteOpenHelper;
    this.storiesApi = storiesApi;
    this.commentsApi = commentsApi;
    this.serviceHandlerInteraction = serviceHandlerInteraction;
  }

  @Override public void handleMessage(Message msg) {
    initDatabaseIfNeeded();
    switch (msg.what) {
      case DOWNLOAD_TOP_STORIES: {
        if (topStoryProcessor != null){
          break;
        }
        TopStoryGateway topStoryGateway = new TopStoryGateway.SQLiteTopStoryGateway(sqLiteDatabase);
        StoryGateway storyGateway = new StoryGateway.SqliteStoryGateway(sqLiteDatabase);
        StoryCommentGateway storyCommentGateway = new SQLiteStoryCommentGateway(sqLiteDatabase);
        topStoryProcessor =
            new TopStoryProcessor(new StoryApiHelper.StoryApiHelperImpl(storiesApi), storyGateway, topStoryGateway, storyCommentGateway,
                new TopStoryProcessor.Callback() {
                  @Override public void onComplete() {
                    topStoryProcessor = null;
                    stopServiceIfNeeded();
                  }
                });
        topStoryProcessor.execute();
        break;
      }

      case DOWNLOAD_COMMENT_FOR_STORY:
        final int storyId = (Integer) msg.obj;
        StoryCommentGateway storyCommentGateway = new SQLiteStoryCommentGateway(sqLiteDatabase);
        CommentGateway commentGateway = new CommentGateway.SQLiteCommentGateway(sqLiteDatabase);
        Command commentProcessor =
            new CommentProcessor(storyId, storyCommentGateway, commentGateway, commentsApi,
                new CommentProcessor.Callback() {
                  @Override public void onComplete() {
                    commentCommands.put(storyId, null);
                    stopServiceIfNeeded();
                  }
                });
        commentProcessor.execute();
        commentCommands.put(storyId, commentProcessor);
        break;

      default:
        throw new UnsupportedOperationException();
    }
  }

  private void stopServiceIfNeeded() {
    boolean isAllEmpty = true;

    if (topStoryProcessor == null) {
      serviceHandlerInteraction.notifyLoadingTopStoryComplete();
    } else {
      isAllEmpty = false;
    }
    for (int i = 0; i < commentCommands.size(); i++) {
      int keyAtI = commentCommands.keyAt(i);
      if (commentCommands.get(keyAtI) == null) {
        serviceHandlerInteraction.notifyLoadingCommentComplete(keyAtI);
      } else {
        isAllEmpty = false;
      }
    }

    if (isAllEmpty) {
      if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
        sqLiteDatabase.close();
      }
      serviceHandlerInteraction.notifyStop();
    }
  }

  @WorkerThread private void initDatabaseIfNeeded() {
    if (sqLiteDatabase == null) {
      sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }
  }
}

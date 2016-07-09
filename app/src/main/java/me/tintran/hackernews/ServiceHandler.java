package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.CommentContract;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.StoryCommentContract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
  private final StopListener stopListener;
  private List<Call> callList = new ArrayList<>();

  ServiceHandler(Looper looper, SQLiteOpenHelper sqLiteOpenHelper, HackerNewsApi.Stories storiesApi,
      HackerNewsApi.Comments commentsApi,
      StopListener stopListener) {
    super(looper);
    this.sqLiteOpenHelper = sqLiteOpenHelper;
    this.storiesApi = storiesApi;
    this.commentsApi = commentsApi;
    this.stopListener = stopListener;
  }

  @Override public void handleMessage(Message msg) {
    initDatabaseIfNeeded();
    switch (msg.what) {
      case DOWNLOAD_TOP_STORIES: {

        Call<int[]> topStories = storiesApi.getTopStories();
        int[] topStoryIds = null;
        try {
          topStoryIds = topStories.execute().body();
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (topStoryIds == null) {
          return;
        }

        TopStoryGateway topStoryGateway = new TopStoryGateway.SQLiteTopStoryGateway(sqLiteDatabase);
        topStoryGateway.replaceTopStoryIds(topStoryIds);

        // Retrieve and insert the stories
        for (final int itemId : topStoryIds) {
          final Call<HackerNewsApi.StoryItem> storyItemCall = storiesApi.getStory(itemId);
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
        break;
      }

      case DOWNLOAD_COMMENT_FOR_STORY:
        String storyId = (String) msg.obj;
        Cursor query =
            sqLiteDatabase.query(StoryCommentContract.StoryCommentColumns.TABLE_NAME, null,
                StoryCommentContract.StoryCommentColumns.COLUMN_NAME_STORYID + " = ? ",
                new String[] { storyId }, null, null, null);
        int count = query.getCount();
        Log.d("CommentDownloadService", "loading comments " + count + " items");
        for (int i = 0; i < query.getCount(); i++) {
          query.moveToPosition(i);
          final int commentId = query.getInt(
              query.getColumnIndex(StoryCommentContract.StoryCommentColumns.COLUMN_NAME_COMMENTID));
          final Call<HackerNewsApi.CommentItem> comment = commentsApi.getComment(commentId);

          comment.enqueue(new retrofit2.Callback<HackerNewsApi.CommentItem>() {
            @Override public void onResponse(Call<HackerNewsApi.CommentItem> call,
                Response<HackerNewsApi.CommentItem> response) {
              HackerNewsApi.CommentItem commentItem = response.body();
              ContentValues contentValues = new ContentValues();
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_BY, commentItem.by);
              contentValues.put(CommentContract.CommentColumns._ID, commentItem.id);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_PARENT,
                  commentItem.parent);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TEXT, commentItem.text);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TIME, commentItem.time);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TYPE, commentItem.type);
              contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TYPE, commentItem.type);

              sqLiteDatabase.insertWithOnConflict(CommentContract.CommentColumns.TABLE_NAME, null,
                  contentValues, SQLiteDatabase.CONFLICT_REPLACE);
              callList.remove(call);
              Log.d("CommentDownloadService", "Done loading comment" + commentId);
              stopServiceIfNeeded();
            }

            @Override public void onFailure(Call<HackerNewsApi.CommentItem> call, Throwable t) {
              callList.remove(call);
              Log.d("CommentDownloadService", "failed loading " + commentId);
              stopServiceIfNeeded();
            }
          });
        }

        query.close();
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void stopServiceIfNeeded() {
    if (callList.isEmpty()) {
      if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
        sqLiteDatabase.close();
      }
      stopListener.notifyStop();
    }
  }

  @WorkerThread private void initDatabaseIfNeeded() {
    if (sqLiteDatabase == null) {
      sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }
  }
}

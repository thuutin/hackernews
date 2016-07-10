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
import android.util.SparseArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.CommentContract.CommentColumns;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.data.StoryCommentContract;
import me.tintran.hackernews.data.StoryContract;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by tin on 7/9/16.
 */

public final class ServiceHandler extends Handler {

  static final int DOWNLOAD_TOP_STORIES = -1;
  static final int DOWNLOAD_COMMENT_FOR_STORY = 10002;

  private SQLiteDatabase sqLiteDatabase;
  private final SQLiteOpenHelper sqLiteOpenHelper;
  private HackerNewsApi.Stories storiesApi;
  private HackerNewsApi.Comments commentsApi;
  private final StopListener stopListener;
  private SparseArray<List<Call>> callList = new SparseArray<>();

  ServiceHandler(Looper looper, SQLiteOpenHelper sqLiteOpenHelper, HackerNewsApi.Stories storiesApi,
      HackerNewsApi.Comments commentsApi, StopListener stopListener) {
    super(looper);
    this.sqLiteOpenHelper = sqLiteOpenHelper;
    this.storiesApi = storiesApi;
    this.commentsApi = commentsApi;
    this.stopListener = stopListener;
    callList.put(DOWNLOAD_COMMENT_FOR_STORY, new ArrayList<Call>());
    callList.put(DOWNLOAD_TOP_STORIES, new ArrayList<Call>());
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

        Call<int[]> updatedStoriesCall = storiesApi.getUpdatedStories();
        int[] updatedStories = null;
        try {
          updatedStories = updatedStoriesCall.execute().body();
        } catch (IOException e) {
          e.printStackTrace();
        }
        Cursor storiesIdsFromDatabase = sqLiteDatabase.query(StoryContract.StoryColumns.TABLE_NAME,
            new String[] { StoryContract.StoryColumns._ID }, null, null, null, null, null);
        storiesIdsFromDatabase.close();
        StoryHelper storyHelper = new StoryHelper(topStoryIds, updatedStories, null);
        // Retrieve and insert the stories
        for (final int itemId : storyHelper.getIdsToRetrieve()) {
          final Call<HackerNewsApi.StoryItem> storyItemCall = storiesApi.getStory(itemId);
          final StoryGateway storyGateway = new StoryGateway.SqliteStoryGateway(sqLiteDatabase);
          StoryCommentGateway.SQLiteStoryCommentGateway storyCommentGateway =
              new StoryCommentGateway.SQLiteStoryCommentGateway(sqLiteDatabase);
          TopStoriesCallback callback =
              new TopStoriesCallback(itemId, storyGateway, storyCommentGateway,
                  new TopStoriesCallback.OnReturn<Call<HackerNewsApi.StoryItem>>() {
                    @Override public void onReturn(Call<HackerNewsApi.StoryItem> call) {
                      callList.get(DOWNLOAD_TOP_STORIES).remove(call);
                      stopServiceIfNeeded();
                    }
                  });
          callList.get(DOWNLOAD_TOP_STORIES).add(storyItemCall);
          storyItemCall.enqueue(callback);
        }
        break;
      }

      case DOWNLOAD_COMMENT_FOR_STORY:
        final Integer storyId = (Integer) msg.obj;
        Cursor query =
            sqLiteDatabase.query(StoryCommentContract.StoryCommentColumns.TABLE_NAME, null,
                StoryCommentContract.StoryCommentColumns.COLUMN_NAME_STORYID + " = ? ",
                new String[] { storyId.toString() }, null, null, null);
        final int count = query.getCount();
        Log.d("CommentDownloadService", "loading comments " + count + " items");
        for (int i = 0; i < count; i++) {
          query.moveToPosition(i);
          final int commentId = query.getInt(
              query.getColumnIndex(StoryCommentContract.StoryCommentColumns.COLUMN_NAME_COMMENTID));
          final Call<HackerNewsApi.CommentItem> comment = commentsApi.getComment(commentId);
          List<Call> calls = callList.get(storyId);
          if (calls == null) {
            calls = new ArrayList<>(count);
            callList.put(storyId, calls);
          }
          calls.add(comment);
          comment.enqueue(new retrofit2.Callback<HackerNewsApi.CommentItem>() {
            @Override public void onResponse(Call<HackerNewsApi.CommentItem> call,
                Response<HackerNewsApi.CommentItem> response) {
              HackerNewsApi.CommentItem commentItem = response.body();
              ContentValues contentValues = new ContentValues();
              contentValues.put(CommentColumns.COLUMN_NAME_BY, commentItem.by);
              contentValues.put(CommentColumns._ID, commentItem.id);
              contentValues.put(CommentColumns.COLUMN_NAME_PARENT, commentItem.parent);
              contentValues.put(CommentColumns.COLUMN_NAME_TEXT, commentItem.text);
              contentValues.put(CommentColumns.COLUMN_NAME_TIME, commentItem.time);
              contentValues.put(CommentColumns.COLUMN_NAME_TYPE, commentItem.type);
              contentValues.put(CommentColumns.COLUMN_NAME_TYPE, commentItem.type);
              callList.get(storyId).remove(call);
              sqLiteDatabase.insertWithOnConflict(CommentColumns.TABLE_NAME, null, contentValues,
                  SQLiteDatabase.CONFLICT_REPLACE);
              Log.d("CommentDownloadService", "Done loading comment" + commentId);
              stopServiceIfNeeded();
            }

            @Override public void onFailure(Call<HackerNewsApi.CommentItem> call, Throwable t) {
              callList.get(storyId).remove(call);
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
    int size = callList.size();
    boolean isAllEmpty = true;
    for (int i = 0; i < size; i++) {
      int keyAtI = callList.keyAt(i);
      if (!callList.get(keyAtI).isEmpty()) {
        isAllEmpty = false;
      } else {
        if (keyAtI == DOWNLOAD_TOP_STORIES) {
          stopListener.notifyLoadingTopStoryComplete();
        } else {
          stopListener.notifyLoadingCommentComplete(keyAtI);
        }
      }
    }

    if (isAllEmpty) {
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

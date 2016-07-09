package me.tintran.hackernews;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.SQLiteDbHelper;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.StoryContract;
import me.tintran.hackernews.data.TopStoriesContract;
import me.tintran.hackernews.data.TopStoriesUseCase;
import me.tintran.hackernews.data.CommentListUseCase;

/**
 * Created by tin on 7/6/16.
 */

public class StoriesRepository implements TopStoriesUseCase, CommentListUseCase {

  private Context context;

  public StoriesRepository(Context context) {
    this.context = context;
  }

  @Override public void getTopStories(final TopStoriesUseCase.Callback callback) {
    new GetTopStoriesAsyncTask(context, callback).execute();
  }

  @Override public void getCommentList(int storyId, final CommentListUseCase.Callback callback) {
    GetCommentListAsyncTask getCommentListAsyncTask =
        new GetCommentListAsyncTask(context, callback);
    getCommentListAsyncTask.execute(storyId);
  }

  public static class GetTopStoriesAsyncTask extends AsyncTask<Void, Void, List<Story>> {
    private WeakReference<Context> context;
    private final WeakReference<TopStoriesUseCase.Callback> callback;

    public GetTopStoriesAsyncTask(Context context, TopStoriesUseCase.Callback callback) {
      this.context = new WeakReference<>(context);
      this.callback = new WeakReference<>(callback);
    }

    @Override protected void onPreExecute() {
      if (context.get() == null) {
        cancel(true);
      }
      super.onPreExecute();
    }

    @Override protected List<Story> doInBackground(Void... params) {
      if (context.get() == null) {
        return null;
      }
      SQLiteDbHelper SQLiteDbHelper = new SQLiteDbHelper(context.get());
      SQLiteDatabase readableDatabase = SQLiteDbHelper.getReadableDatabase();
      String tableName = TopStoriesContract.StoryColumns.TABLE_NAME +
          " LEFT JOIN " + StoryContract.StoryColumns.TABLE_NAME +
          " ON " + StoryContract.StoryColumns.TABLE_NAME + "." + StoryContract.StoryColumns._ID + " = " + TopStoriesContract.StoryColumns.STORYID;

      Cursor query = readableDatabase.query(tableName, null, null, null, null, null,
          TopStoriesContract.StoryColumns.TABLE_NAME + "." + TopStoriesContract.StoryColumns._ID + " ASC");

      int size = query.getCount();
      List<Story> results = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        query.moveToPosition(i);
        Story story = new Story(query.getInt(query.getColumnIndex(TopStoriesContract.StoryColumns.STORYID)),
            query.getString(query.getColumnIndex(StoryContract.StoryColumns.COLUMN_NAME_TITLE)),
            query.getString(query.getColumnIndex(TopStoriesContract.StoryColumns.STORYID)));
        results.add(story);
      }
      query.close();
      readableDatabase.close();
      return results;
    }

    @Override protected void onPostExecute(List<Story> stories) {
      if (context.get() == null || callback.get() == null) {
        return;
      }

      if (stories == null ){
        callback.get().onError(0);
      } else {
        callback.get().onComplete(stories);
      }
    }
  }
}

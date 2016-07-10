package me.tintran.hackernews;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import java.util.List;
import me.tintran.hackernews.data.SQLiteDbHelper;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.TopStoriesUseCase;

/**
 * Created by tin on 7/10/16.
 */
public class GetTopStoriesAsyncTask extends AsyncTask<Void, Void, List<Story>> {
  private WeakReference<Context> context;
  private final WeakReference<TopStoriesUseCase.Callback> callback;

  public GetTopStoriesAsyncTask(Context context, TopStoriesUseCase.Callback callback) {
    this.context = new WeakReference<>(context);
    this.callback = new WeakReference<>(callback);
  }

  @Override protected List<Story> doInBackground(Void... params) {
    if (context.get() == null) {
      return null;
    }
    SQLiteDbHelper SQLiteDbHelper = new SQLiteDbHelper(context.get());
    SQLiteDatabase readableDatabase = SQLiteDbHelper.getReadableDatabase();
    StoryGateway storyGateway = new StoryGateway.SqliteStoryGateway(readableDatabase);
    return storyGateway.getTopStories();
  }

  @Override protected void onPostExecute(List<Story> stories) {
    if (context.get() == null || callback.get() == null) {
      return;
    }

    if (stories == null) {
      callback.get().onError(0);
    } else {
      callback.get().onComplete(stories);
    }
  }
}
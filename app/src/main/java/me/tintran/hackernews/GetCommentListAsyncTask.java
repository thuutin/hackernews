package me.tintran.hackernews;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.Html;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.data.CommentContract;
import me.tintran.hackernews.data.CommentContract.CommentColumns;
import me.tintran.hackernews.data.CommentGateway;
import me.tintran.hackernews.data.CommentListUseCase;
import me.tintran.hackernews.data.SQLiteDbHelper;
import me.tintran.hackernews.data.StoryCommentContract;
import me.tintran.hackernews.data.StoryCommentContract.StoryCommentColumns;
import me.tintran.hackernews.storydetail.Comment;

/**
 * Created by tin on 7/8/16.
 */
class GetCommentListAsyncTask extends AsyncTask<Integer, Void, List<Comment>> {

  private final WeakReference<CommentListUseCase.Callback> callback;
  private final WeakReference<Context> context;

  GetCommentListAsyncTask(Context context, CommentListUseCase.Callback callback) {
    this.context = new WeakReference<>(context);
    this.callback = new WeakReference<>(callback);
  }

  @Override protected List<Comment> doInBackground(Integer... params) {
    if (context.get() == null){
      return null;
    }
    final int storyId = params[0];
    SQLiteDbHelper sqLiteDbHelper = new SQLiteDbHelper(context.get());
    CommentGateway commentGateway = new CommentGateway.SQLiteCommentGateway(sqLiteDbHelper.getReadableDatabase());
    return commentGateway.getCommentsForStory(storyId);
  }

  @Override protected void onPostExecute(List<Comment> comments) {
    if (context.get() == null || callback.get() == null) {
      return;
    }
    if (comments == null) {
      callback.get().onError(1);
    } else {
      callback.get().onComplete(comments);
    }
  }
}

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
import me.tintran.hackernews.data.CommentListUseCase;
import me.tintran.hackernews.data.SQLiteDbHelper;
import me.tintran.hackernews.data.StoryCommentContract;
import me.tintran.hackernews.data.StoryCommentContract.StoryCommentColumns;
import me.tintran.hackernews.storydetail.Comment;

/**
 * Created by tin on 7/8/16.
 */
public class GetCommentListAsyncTask extends AsyncTask<Integer, Void, List<Comment>> {

  private final WeakReference<CommentListUseCase.Callback> callback;
  private final WeakReference<Context> context;

  public GetCommentListAsyncTask(Context context, CommentListUseCase.Callback callback) {
    this.callback = new WeakReference<>(callback);
    this.context = new WeakReference<>(context);
  }

  @Override protected void onPreExecute() {
    super.onPreExecute();
  }

  @Override protected List<Comment> doInBackground(Integer... params) {
    if (context.get() == null) {
      return null;
    }
    final int storyId = params[0];
    SQLiteDbHelper SQLiteDbHelper = new SQLiteDbHelper(context.get());
    SQLiteDatabase readableDatabase = SQLiteDbHelper.getReadableDatabase();
    String[] projection = {
        CommentColumns.COLUMN_NAME_TEXT, CommentColumns.TABLE_NAME + "." + CommentColumns._ID
    };
    Cursor query = readableDatabase.query(CommentColumns.TABLE_NAME
            + " JOIN "
            + StoryCommentColumns.TABLE_NAME
            + " ON "
            + CommentColumns.TABLE_NAME
            + "."
            + CommentColumns._ID
            + " = "
            + StoryCommentColumns.COLUMN_NAME_COMMENTID, projection, StoryCommentColumns.COLUMN_NAME_STORYID + " = ? ", new String[] { String.valueOf(storyId) },
        null, null, CommentColumns.COLUMN_NAME_TIME + " ASC ", null);
    List<Comment> comments = new ArrayList<>(query.getCount());
    for (int i = 0; i < query.getCount(); i++) {
      query.moveToPosition(i);
      final int commentId = query.getInt(query.getColumnIndex(CommentColumns._ID));
      final String commentText =
          query.getString(query.getColumnIndex(CommentColumns.COLUMN_NAME_TEXT));
      Comment comment = new Comment(commentId,
          commentText == null ? null : Html.fromHtml(commentText).toString());
      comments.add(comment);
    }
    query.close();
    return comments;
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

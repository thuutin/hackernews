package me.tintran.hackernews.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.storydetail.Comment;

/**
 * Created by tin on 7/10/16.
 */

public interface CommentGateway {

  void insertComment(String by, int id, int parent, String text, long time, String type, boolean deleted);

  List<Comment> getCommentsForStory(int storyId);

  class SQLiteCommentGateway implements CommentGateway {

    private SQLiteDatabase sqLiteDatabase;

    public SQLiteCommentGateway(SQLiteDatabase sqLiteDatabase) {
      this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override public void insertComment(String by, int id, int parent, String text, long time,
        String type, boolean deleted) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_BY, by);
      contentValues.put(CommentContract.CommentColumns._ID, id);
      contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_PARENT, parent);
      contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TEXT, text);
      contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TIME, time);
      contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_DELETED, deleted);
      contentValues.put(CommentContract.CommentColumns.COLUMN_NAME_TYPE, type);
      sqLiteDatabase.insertWithOnConflict(CommentContract.CommentColumns.TABLE_NAME, null, contentValues,
          SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override public List<Comment> getCommentsForStory(int storyId) {
      // Checking whether this story has any Comments;
      // If this cursor has count > 0, then this story has comment but not downloaded yet
      Cursor commentByStory =
          sqLiteDatabase.query(StoryCommentContract.StoryCommentColumns.TABLE_NAME, null, StoryCommentContract.StoryCommentColumns.COLUMN_NAME_STORYID + " = ?" ,
              new String[] {String.valueOf(storyId)}, null, null, null);
      if (commentByStory.getCount() == 0){
        commentByStory.close();
        return null;
      }


      String[] projection = {
          CommentContract.CommentColumns.COLUMN_NAME_TEXT, CommentContract.CommentColumns.TABLE_NAME + "." + CommentContract.CommentColumns._ID
      };
      final String selection = StoryCommentContract.StoryCommentColumns.COLUMN_NAME_STORYID
          + " = ?  AND "
          + CommentContract.CommentColumns.COLUMN_NAME_DELETED
          + " = ?";
      Cursor query = sqLiteDatabase.query(CommentContract.CommentColumns.TABLE_NAME
              + " JOIN "
              + StoryCommentContract.StoryCommentColumns.TABLE_NAME
              + " ON "
              + CommentContract.CommentColumns.TABLE_NAME
              + "."
              + CommentContract.CommentColumns._ID
              + " = "
              + StoryCommentContract.StoryCommentColumns.COLUMN_NAME_COMMENTID, projection, selection,
          new String[] { String.valueOf(storyId), String.valueOf(0) }, null, null,
          CommentContract.CommentColumns.COLUMN_NAME_TIME + " ASC ", null);
      List<Comment> comments = new ArrayList<>(query.getCount());
      for (int i = 0; i < query.getCount(); i++) {
        query.moveToPosition(i);
        final int commentId = query.getInt(query.getColumnIndex(CommentContract.CommentColumns._ID));
        final String commentText =
            query.getString(query.getColumnIndex(CommentContract.CommentColumns.COLUMN_NAME_TEXT));
        Comment comment = new Comment(commentId,
            commentText == null ? null : Html.fromHtml(commentText).toString());
        comments.add(comment);
      }
      query.close();
      sqLiteDatabase.close();
      return comments;
    }
  }
}

package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import me.tintran.hackernews.data.StoryCommentContract;

/**
 * Created by tin on 7/9/16.
 */
public interface StoryCommentGateway {
  void insert(int storyId, int[] commentIds);

  public class SqliteStoryCommentGateway implements StoryCommentGateway {

    private SQLiteDatabase sqLiteDatabase;

    public SqliteStoryCommentGateway(SQLiteDatabase sqLiteDatabase) {
      this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override public void insert(int storyId, int[] commentIds) {

      if (commentIds == null || commentIds.length == 0){
        return;
      }
      ContentValues contentValues = new ContentValues();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        sqLiteDatabase.beginTransactionNonExclusive();
      } else {
        sqLiteDatabase.beginTransaction();
      }
      try {
        contentValues.put(StoryCommentContract.StoryCommentColumns.COLUMN_NAME_STORYID, storyId);
        for (int i = 0; i < commentIds.length; i++) {
          contentValues.put(StoryCommentContract.StoryCommentColumns.COLUMN_NAME_COMMENTID,
              commentIds[i]);
          sqLiteDatabase.insertWithOnConflict(StoryCommentContract.StoryCommentColumns.TABLE_NAME,
              null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        sqLiteDatabase.setTransactionSuccessful();
      } finally {
        sqLiteDatabase.endTransaction();
      }
    }
  }
}

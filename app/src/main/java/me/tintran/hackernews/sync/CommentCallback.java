package me.tintran.hackernews.sync;

import android.util.Log;
import me.tintran.hackernews.data.CommentGateway;
import me.tintran.hackernews.data.HackerNewsApi;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by tin on 7/10/16.
 */

class CommentCallback implements retrofit2.Callback<HackerNewsApi.CommentItem> {
  private final int commentId;
  private CommentGateway commentGateway;
  private OnReturn onReturn;

  public CommentCallback(int commentId, CommentGateway commentGateway, OnReturn onReturn) {
    this.commentId = commentId;
    this.commentGateway = commentGateway;
    this.onReturn = onReturn;
  }

  @Override public void onResponse(Call<HackerNewsApi.CommentItem> call,
      Response<HackerNewsApi.CommentItem> response) {
    HackerNewsApi.CommentItem commentItem = response.body();
    commentGateway.insertComment(commentItem.by, commentItem.id, commentItem.parent,
        commentItem.text, commentItem.time, commentItem.type, commentItem.deleted);
    onReturn.onReturn(call);
  }

  @Override public void onFailure(Call<HackerNewsApi.CommentItem> call, Throwable t) {
    Log.d("CommentDownloadService", "failed loading " + commentId);
    onReturn.onReturn(call);
  }

  public interface OnReturn {
    void onReturn(Call<HackerNewsApi.CommentItem> commentItemCall);
  }
}
package me.tintran.hackernews.storydetail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tin on 7/9/16.
 */
public class CommentDownloadCompleteBroadcastReceiver extends BroadcastReceiver {

  private ReloadComment reloadComment;

  public CommentDownloadCompleteBroadcastReceiver(ReloadComment reloadComment) {
    this.reloadComment = reloadComment;
  }

  @Override public void onReceive(Context context, Intent intent) {
    reloadComment.loadComments();
  }

  interface ReloadComment {
    void loadComments();
  }
}

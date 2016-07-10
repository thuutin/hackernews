package me.tintran.hackernews.sync;

import android.util.Log;
import java.util.ArrayList;
import me.tintran.hackernews.StoryCommentGateway;
import me.tintran.hackernews.data.CommentGateway;
import me.tintran.hackernews.data.HackerNewsApi;
import retrofit2.Call;

/**
 * Created by tin on 7/10/16.
 */
public class CommentProcessor implements Command {
  private final int storyId;
  private final StoryCommentGateway storyCommentGateway;
  private final CommentGateway commentGateway;
  private HackerNewsApi.Comments commentsApi;
  private Callback callback;
  private ArrayList<Object> callList;

  public CommentProcessor(int storyId, StoryCommentGateway storyCommentGateway,
      CommentGateway commentGateway, HackerNewsApi.Comments commentsApi, Callback callback) {
    this.storyId = storyId;
    this.storyCommentGateway = storyCommentGateway;
    this.commentGateway = commentGateway;
    this.commentsApi = commentsApi;
    this.callback = callback;
  }

  @Override public void execute() {
    downloadComments();
  }

  private void downloadComments(){
    int[] commentIds = storyCommentGateway.getCommentIdsByStoryId(storyId);
    Log.d("CommentDownloadService", "loading comments " + commentIds.length + " items");
    callList = new ArrayList<>(commentIds.length);
    for (int i = 0; i < commentIds.length; i++) {
      final int commentId = commentIds[i];
      final Call<HackerNewsApi.CommentItem> comment = commentsApi.getComment(commentId);
      callList.add(comment);
      CommentCallback commentCallback = new CommentCallback(commentId, commentGateway, new CommentCallback.OnReturn(){
        @Override public void onReturn(Call<HackerNewsApi.CommentItem> commentItemCall) {
          callList.remove(commentItemCall);
          if (callList.isEmpty()){
            callback.onComplete();
          }
        }
      });
      comment.enqueue(commentCallback);
    }
  }

  public interface Callback {
    void onComplete();
  }

}

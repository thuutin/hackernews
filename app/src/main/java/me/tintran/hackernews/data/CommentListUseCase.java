package me.tintran.hackernews.data;

import java.util.List;
import me.tintran.hackernews.storydetail.Comment;

/**
 * Created by tin on 7/7/16.
 */
public interface CommentListUseCase {
  void getCommentList(int storyId, CommentListUseCase.Callback callback);

  int getErrorString(int code);

  interface Callback {
    void onComplete(List<Comment> commentList);
    void onError(int code);
  }
}

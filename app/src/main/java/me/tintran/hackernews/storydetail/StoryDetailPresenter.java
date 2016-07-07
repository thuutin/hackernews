package me.tintran.hackernews.storydetail;

import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.CommentListUseCase;

/**
 * Created by tin on 7/7/16.
 */

class StoryDetailPresenter implements StoryDetailContract.ActionsListener {

  private StoryDetailContract.View view;
  private int storyId;
  private CommentListUseCase commentListUseCase;

  StoryDetailPresenter(int storyId, CommentListUseCase commentListUseCase) {
    this.storyId = storyId;
    this.commentListUseCase = commentListUseCase;
  }

  @Override public void attachView(StoryDetailContract.View view) {
    this.view = view;
    view.showStatusText(R.string.loading);
    commentListUseCase.getCommentList(storyId, new CommentListUseCase.Callback() {
      @Override public void onComplete(List<Comment> commentList) {
        StoryDetailContract.View v = StoryDetailPresenter.this.view;
        if (v == null){
          return;
        }
        v.showCommentList(commentList);
      }

      @Override public void onError(int code) {
        StoryDetailContract.View v = StoryDetailPresenter.this.view;
        if (v == null){
          return;
        }
        v.showStatusText(R.string.error_load_comments);
      }
    });
  }

  @Override public void detachView() {

  }
}

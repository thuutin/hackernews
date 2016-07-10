package me.tintran.hackernews.storydetail;

import android.support.annotation.StringRes;
import java.util.List;

/**
 * Created by tin on 7/7/16.
 */

public interface StoryDetailContract {

  interface View {

    void showStatusText(@StringRes int statusRes);

    void showCommentList(List<Comment> comments);

    void showLoading();
    void hideLoading();
  }

  interface ActionsListener {

    void detachView();

    void attachView(View view);

    void loadComments();
  }
}

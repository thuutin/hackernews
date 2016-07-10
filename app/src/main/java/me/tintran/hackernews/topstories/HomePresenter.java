package me.tintran.hackernews.topstories;

import android.support.annotation.NonNull;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.TopStoriesUseCase;

/**
 * Created by tin on 7/6/16.
 */

final class HomePresenter implements HomeContract.ActionsListener, TopStoriesUseCase.Callback {

  private TopStoriesUseCase topStoriesUseCase;
  private HomeContract.View view;

  HomePresenter(TopStoriesUseCase topStoriesUseCase) {
    this.topStoriesUseCase = topStoriesUseCase;
  }

  @Override public void attachView(HomeContract.View view) {
    this.view = view;
  }

  @Override public void detachView() {
    view = null;
  }

  @Override public void onComplete(@NonNull List<Story> stories) {
    if (view == null){
      return;
    }
    if (stories.size() == 0) {
      view.showStatusText(R.string.loading_stories);
      view.hideLoading();
    } else {
      view.showItem(stories);
      view.hideStatusText();
      view.hideLoading();
    }
  }

  @Override public void onResume() {
    view.registerReceiver();
  }

  @Override public void onPause() {
    view.unregisterReceiver();
  }

  @Override public void loadTopStories() {
    view.showLoading();
    topStoriesUseCase.getTopStories(this);
  }

  @Override public void onError(int code) {
    if (view == null){
      return;
    }
    final int stringRes = R.string.error_load_stories;
    view.hideLoading();
    view.showStatusText(stringRes);
  }

  @Override public void onSwipeToRefresh() {
    view.refreshTopStories();
  }

  @Override public void onStoryClicked(Story story) {
    view.gotoStoryDetail(story);
  }
}

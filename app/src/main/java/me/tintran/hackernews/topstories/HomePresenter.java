package me.tintran.hackernews.topstories;

import java.util.List;
import me.tintran.hackernews.R;

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
    topStoriesUseCase.getTopStories(this);
  }

  @Override public void detachView() {
    view = null;
  }

  @Override public void onComplete(List<Item> items) {
    view.hideStatusText();
    view.showItem(items);
  }

  @Override public void onError(int code) {
    final int stringRes = R.string.no_stories;
    view.showStatusText(stringRes);
  }
}

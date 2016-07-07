package me.tintran.hackernews.topstories;

import android.support.annotation.NonNull;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Item;
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
    topStoriesUseCase.getTopStories(this);
  }

  @Override public void detachView() {
    view = null;
  }

  @Override public void onComplete(@NonNull List<Item> items) {
    if (items.size() == 0) {
      view.showStatusText(R.string.no_stories);
    } else {
      view.showItem(items);
      view.hideStatusText();
    }
  }

  @Override public void onError(int code) {
    final int stringRes = R.string.error_load_stories;
    view.showStatusText(stringRes);
  }

  @Override public void onStoryClicked(Item item) {
    view.gotoStoryDetail(item);
  }
}

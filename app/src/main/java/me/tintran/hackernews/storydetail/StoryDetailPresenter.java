package me.tintran.hackernews.storydetail;

/**
 * Created by tin on 7/7/16.
 */

public class StoryDetailPresenter implements StoryDetailContract.ActionsListener {

  private StoryDetailContract.View view;

  public StoryDetailPresenter() {
  }



  @Override public void attachView(StoryDetailContract.View view) {
    this.view = view;
  }

  @Override public void detachView() {

  }
}

package me.tintran.hackernews;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import java.util.List;
import me.tintran.hackernews.data.Item;
import me.tintran.hackernews.data.TopStoriesUseCase;

/**
 * Created by tin on 7/6/16.
 */

public class StoriesRepository implements
    TopStoriesUseCase, LoaderManager.LoaderCallbacks<List<Item>> {

  private LoaderManager loaderManager;
  private Callback callback;

  public StoriesRepository(LoaderManager loaderManager) {
    this.loaderManager = loaderManager;
  }

  @Override public void getTopStories(final Callback callback) {
    this.callback = callback;
    loaderManager.initLoader(0, null, this);
  }

  @Override public Loader<List<Item>> onCreateLoader(int id, Bundle args) {
    return null;
  }

  @Override public void onLoadFinished(Loader<List<Item>> loader, List<Item> data) {
    callback.onComplete(data);
    loaderManager.destroyLoader(0);
  }

  @Override public void onLoaderReset(Loader<List<Item>> loader) {

  }
}

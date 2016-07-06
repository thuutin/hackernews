package me.tintran.hackernews;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import me.tintran.hackernews.data.Item;
import me.tintran.hackernews.data.TopStoriesUseCase;

/**
 * Created by tin on 7/6/16.
 */
public class StoriesRepository implements TopStoriesUseCase {

  private final static List<Item> DATA = Arrays.asList(
      new Item("Title 1", "Subtitle 1"),
      new Item("Title 2", "Subtitle 2"),
      new Item("Title 3", "Subtitle 3"),
      new Item("Title 4", "Subtitle 4"),
      new Item("Title 5", "Subtitle 5"),
      new Item("Title 6", "Subtitle 6"),
      new Item("Title 7", "Subtitle 7")
  );

  public StoriesRepository(LoaderManager loaderManager) {
  }

  @Override public void getTopStories(final Callback callback) {
    Handler handler = new Handler();
    handler.postAtTime(new TimerTask() {
      @Override public void run() {
        callback.onComplete(DATA);
      }
    }, 5000);
  }
}

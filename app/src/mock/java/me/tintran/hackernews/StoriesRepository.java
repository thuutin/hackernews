package me.tintran.hackernews;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.TopStoriesUseCase;

/**
 * Created by tin on 7/6/16.
 */
public class StoriesRepository implements TopStoriesUseCase {

  private final static List<Story> DATA = Arrays.asList(
      new Story(1, "Title 1", "Subtitle 1"),
      new Story(2, "Title 2", "Subtitle 2"),
      new Story(3, "Title 3", "Subtitle 3"),
      new Story(4, "Title 4", "Subtitle 4"),
      new Story(5, "Title 5", "Subtitle 5"),
      new Story(6, "Title 6", "Subtitle 6"),
      new Story(7, "Title 7", "Subtitle 7")
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

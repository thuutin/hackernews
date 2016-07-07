package me.tintran.hackernews;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import me.tintran.hackernews.data.CommentListUseCase;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.TopStoriesUseCase;
import me.tintran.hackernews.storydetail.Comment;

/**
 * Created by tin on 7/6/16.
 */
public class StoriesRepository implements TopStoriesUseCase, CommentListUseCase {

  private final static List<Story> STORIES = Arrays.asList(
      new Story(1, "Title 1", "Subtitle 1"),
      new Story(2, "Title 2", "Subtitle 2"),
      new Story(3, "Title 3", "Subtitle 3"),
      new Story(4, "Title 4", "Subtitle 4"),
      new Story(5, "Title 5", "Subtitle 5"),
      new Story(6, "Title 6", "Subtitle 6"),
      new Story(7, "Title 7", "Subtitle 7")
  );

  private final static List<Comment> COMMENTS = Arrays.asList(
      new Comment(1, "Comment 1"),
      new Comment(2, "Comment 2"),
      new Comment(3, "Comment 3"),
      new Comment(4, "Comment 4"),
      new Comment(5, "Comment 5"),
      new Comment(6, "Comment 6"),
      new Comment(7, "Comment 7")
  );
  private Context context;

  public StoriesRepository(Context context) {

    this.context = context;
  }

  @Override public void getCommentList(int storyId, final CommentListUseCase.Callback callback) {
    Handler handler = new Handler();
    handler.postAtTime(new TimerTask() {
      @Override public void run() {
        callback.onComplete(COMMENTS);
      }
    }, 5000);
  }

  @Override public void getTopStories(final TopStoriesUseCase.Callback callback) {
    Handler handler = new Handler();
    handler.postAtTime(new TimerTask() {
      @Override public void run() {
        callback.onComplete(STORIES);
      }
    }, 5000);
  }
}

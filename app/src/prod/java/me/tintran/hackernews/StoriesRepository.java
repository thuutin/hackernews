package me.tintran.hackernews;

import android.content.Context;
import me.tintran.hackernews.data.CommentListUseCase;
import me.tintran.hackernews.data.TopStoriesUseCase;

/**
 * Created by tin on 7/6/16.
 */

public class StoriesRepository implements TopStoriesUseCase, CommentListUseCase {

  private Context context;

  public StoriesRepository(Context context) {
    this.context = context;
  }

  @Override public void getTopStories(final TopStoriesUseCase.Callback callback) {
    new GetTopStoriesAsyncTask(context, callback).execute();
  }

  @Override public void getCommentList(int storyId, final CommentListUseCase.Callback callback) {
    GetCommentListAsyncTask getCommentListAsyncTask =
        new GetCommentListAsyncTask(context, callback);
    getCommentListAsyncTask.execute(storyId);
  }
}

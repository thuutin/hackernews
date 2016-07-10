package me.tintran.hackernews.storydetail;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.StoriesRepository;
import me.tintran.hackernews.sync.SyncService;

/**
 * Created by tin on 7/7/16.
 */
public class StoryDetailActivity extends AppCompatActivity implements StoryDetailContract.View {

  public static final String STORY_ID = "storyId";
  public static final String STORY_TITLE = "StoryTitle";
  private CommentAdapter adapter;
  private StoryDetailContract.ActionsListener actionsListener;
  private int storyId;

  BroadcastReceiver broadcastReceiver;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_storydetail);
    if (!getIntent().hasExtra(STORY_ID)) {
      throw new IllegalStateException("Did not find StoryId in intent extras");
    }
    final String storyTitle = getIntent().getStringExtra(STORY_TITLE);
    storyId = getIntent().getIntExtra(STORY_ID, -1);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    final TextView storyTitleTextView = (TextView) findViewById(R.id.storyTitle);
    storyTitleTextView.setText(storyTitle);

    final RecyclerView commentList = (RecyclerView) findViewById(R.id.commentList);
    adapter = new CommentAdapter();
    adapter.setHasStableIds(true);
    commentList.setAdapter(adapter);
    actionsListener = new StoryDetailPresenter(storyId, new StoriesRepository(this));
    Intent intent = new Intent(this, SyncService.class);
    intent.setAction(SyncService.DOWNLOAD_COMMENT_FOR_STORY);
    intent.putExtra(SyncService.STORY_ID, storyId);
    startService(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    actionsListener.attachView(this);
    actionsListener.loadComments();
  }

  @Override protected void onResume() {
    broadcastReceiver = new CommentDownloadCompleteBroadcastReceiver(new CommentDownloadCompleteBroadcastReceiver.ReloadComment() {
      @Override public void loadComment() {
        actionsListener.loadComments();
      }
    });
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(SyncService.DONE_DOWNLOAD_COMMENTS);
    try {
      intentFilter.addDataType("comment/" + storyId);
    } catch (IntentFilter.MalformedMimeTypeException e) {
      e.printStackTrace();
    }
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    super.onResume();
  }

  @Override protected void onPause() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    super.onPause();
  }

  @Override protected void onStop() {
    super.onStop();
    actionsListener.detachView();
  }


  @Override public void showLoading() {

  }

  @Override public void hideLoading() {

  }

  @Override public void showStatusText(@StringRes int statusRes) {

  }

  @Override public void showCommentList(List<Comment> comments) {
    adapter.swapData(comments);
  }
}

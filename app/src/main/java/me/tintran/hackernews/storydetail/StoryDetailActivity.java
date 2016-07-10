package me.tintran.hackernews.storydetail;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.StoriesRepository;
import me.tintran.hackernews.sync.SyncService;

import static android.view.View.GONE;

/**
 * Created by tin on 7/7/16.
 */
public class StoryDetailActivity extends AppCompatActivity implements StoryDetailContract.View {

  public static final String STORY_ID = "storyId";
  public static final String STORY_TITLE = "StoryTitle";
  private CommentAdapter adapter;
  private StoryDetailContract.ActionsListener actionsListener;
  private int storyId;

  private BroadcastReceiver broadcastReceiver;
  private SwipeRefreshLayout swipeRefreshLayout;
  private TextView statusTextView;
  private RecyclerView commentList;

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

    swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout));
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        startSyncServiceWithDownloadCommentsForStoryAction();
      }
    });
    final TextView storyTitleTextView = (TextView) findViewById(R.id.storyTitle);
    storyTitleTextView.setText(storyTitle);

    statusTextView = (TextView) findViewById(R.id.statusTextView);
    commentList = (RecyclerView) findViewById(R.id.commentList);
    adapter = new CommentAdapter();
    adapter.setHasStableIds(true);
    commentList.setAdapter(adapter);
    actionsListener = new StoryDetailPresenter(storyId, new StoriesRepository(this));
    startSyncServiceWithDownloadCommentsForStoryAction();
  }

  private void startSyncServiceWithDownloadCommentsForStoryAction() {
    Intent intent = new Intent(this, SyncService.class);
    intent.setAction(SyncService.DOWNLOAD_COMMENT_FOR_STORY);
    intent.putExtra(SyncService.STORY_ID, storyId);
    startService(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    actionsListener.attachView(this);
  }

  @Override protected void onResume() {
    actionsListener.onResume();
    super.onResume();
  }

  @Override protected void onPause() {
    actionsListener.onPause();
    super.onPause();
  }

  @Override protected void onStop() {
    super.onStop();
    actionsListener.detachView();
  }

  @Override public void unregisterReceiver() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
  }

  @Override public void registerReceiver() {
    broadcastReceiver = new CommentDownloadCompleteBroadcastReceiver(
        new CommentDownloadCompleteBroadcastReceiver.ReloadComment() {
          @Override public void loadComments() {
            actionsListener.onReceiverFired();
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
  }

  @Override public void showLoading() {
    swipeRefreshLayout.setRefreshing(true);
    commentList.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void showStatusText(@StringRes int statusRes) {
    statusTextView.setText(statusRes);
    statusTextView.setVisibility(View.VISIBLE);
    commentList.setVisibility(View.INVISIBLE);
  }

  @Override public void showCommentList(List<Comment> comments) {
    adapter.swapData(comments);
    statusTextView.setVisibility(GONE);
    commentList.setVisibility(View.VISIBLE);
  }
}

package me.tintran.hackernews.topstories;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.sync.SyncService;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.StoriesRepository;
import me.tintran.hackernews.storydetail.StoryDetailActivity;

public class HomeActivity extends AppCompatActivity implements HomeContract.View {

  private TextView statusText;
  private StoriesAdapter storiesAdapter;
  private HomeContract.ActionsListener actionsListener;
  private RecyclerView storiesList;
  private BroadcastReceiver receiver;
  private SwipeRefreshLayout swipeRefreshLayout;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    statusText = (TextView) findViewById(R.id.statusTextView);
    swipeRefreshLayout =
        ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout));
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        actionsListener.onSwipeToRefresh();
      }
    });
    this.storiesList = (RecyclerView) findViewById(R.id.storiesList);
    storiesAdapter = new StoriesAdapter(new StoriesAdapter.OnStoryClick() {
      @Override public void onClick(Story story) {
        actionsListener.onStoryClicked(story);
      }
    });
    storiesList.setAdapter(storiesAdapter);
    actionsListener = new HomePresenter(new StoriesRepository(this));
    actionsListener.attachView(this);
    actionsListener.loadTopStories();
    startSyncServiceWithUpdateTopStoriesAction();
  }

  @Override protected void onResume() {
    actionsListener.onResume();
    super.onResume();
  }

  @Override protected void onPause() {
    actionsListener.onPause();
    super.onPause();
  }

  @Override public void registerReceiver() {
    receiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        actionsListener.loadTopStories();
      }
    };
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(SyncService.DONE_DOWNLOAD_STORIES);
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
  }

  @Override public void unregisterReceiver() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
  }

  @Override protected void onDestroy() {
    actionsListener.detachView();
    super.onDestroy();
  }

  @Override public void hideStatusText() {
    statusText.setVisibility(View.GONE);
    storiesList.setVisibility(View.VISIBLE);
  }

  @Override public void showStatusText(@StringRes int statusRes) {
    statusText.setText(statusRes);
    statusText.setVisibility(View.VISIBLE);
    storiesList.setVisibility(View.GONE);
  }

  @Override public void showItem(List<Story> itemsList) {
    storiesAdapter.swapData(itemsList);
  }

  @Override public void gotoStoryDetail(Story story) {
    Intent intent = new Intent(HomeActivity.this, StoryDetailActivity.class);
    intent.putExtra(StoryDetailActivity.STORY_ID, story.id);
    intent.putExtra(StoryDetailActivity.STORY_TITLE, story.title);
    startActivity(intent);
  }

  @Override public void hideLoading() {
    swipeRefreshLayout.setRefreshing(false);
  }

  @Override public void refreshTopStories() {
    startSyncServiceWithUpdateTopStoriesAction();
  }

  private void startSyncServiceWithUpdateTopStoriesAction() {
    Intent intent = new Intent(this, SyncService.class);
    intent.setAction(SyncService.UPDATE_TOP_STORIES);
    startService(intent);
  }
}

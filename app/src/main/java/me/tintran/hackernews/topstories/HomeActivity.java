package me.tintran.hackernews.topstories;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.StoriesRepository;
import me.tintran.hackernews.storydetail.StoryDetailActivity;

public class HomeActivity extends AppCompatActivity implements HomeContract.View {

  private TextView statusText;
  private StoriesAdapter storiesAdapter;
  private HomeContract.ActionsListener actionsListener;
  private RecyclerView storiesList;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    statusText = (TextView) findViewById(R.id.statusTextView);

    this.storiesList = (RecyclerView) findViewById(R.id.storiesList);
    storiesAdapter = new StoriesAdapter(new StoriesAdapter.OnStoryClick() {
      @Override public void onClick(Story story) {
        actionsListener.onStoryClicked(story);
      }
    });
    storiesList.setAdapter(storiesAdapter);
    actionsListener = new HomePresenter(new StoriesRepository(getSupportLoaderManager()));
    actionsListener.attachView(this);
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
    startActivity(intent);
  }
}

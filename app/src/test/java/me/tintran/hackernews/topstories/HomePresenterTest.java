package me.tintran.hackernews.topstories;

import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Story;
import me.tintran.hackernews.data.TopStoriesUseCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by tin on 7/6/16.
 */
public class HomePresenterTest {
  final List<Story> mockStories = new ArrayList<>(3);

  HomeContract.ActionsListener homePresenter;
  @Mock HomeContract.View view;
  @Mock TopStoriesUseCase topStoriesUseCase;

  @Captor ArgumentCaptor<TopStoriesUseCase.Callback> callbackArgumentCaptor;
  @Captor ArgumentCaptor<List<Story>> listArgumentCaptor;
  @Captor ArgumentCaptor<Story> itemCaptor;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    homePresenter = new HomePresenter(topStoriesUseCase);
    mockStories.add(new Story(1, "item1", 0, "subtitle 1", 0));
    mockStories.add(new Story(2, "item2", 0, "subtitle 2", 0));
    mockStories.add(new Story(3, "item3", 0,  "subtitle 3",0 ));
  }

  @Test public void doShowItemsOnLoadError() throws Exception {
    homePresenter.attachView(view);
    homePresenter.loadTopStories();
    verify(view).showLoading();
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    int fakeErrorCode = 1001;
    capturedCallback.onError(fakeErrorCode);
    verify(view).showStatusText(R.string.error_load_stories);
    verify(view).hideLoading();
  }

  @Test public void doShowItemsOnLoadSuccess() throws Exception {
    homePresenter.attachView(view);
    homePresenter.loadTopStories();
    verify(view).showLoading();
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    capturedCallback.onComplete(mockStories);
    verify(view).showItem(listArgumentCaptor.capture());
    verify(view).hideLoading();
    List<Story> capturedListStory = listArgumentCaptor.getValue();
    assertEquals(capturedListStory.size(), mockStories.size());
    for (int i = 0; i < capturedListStory.size(); i++) {
      Story expectedStory = mockStories.get(i);
      Story actualStory = capturedListStory.get(i);
      assertTrue(expectedStory == actualStory);
      assertEquals(expectedStory.title, actualStory.title);
    }
  }

  @Test public void doShowItemsOnLoadSuccessWithEmptyList() throws Exception {
    final List<Story> mockStories = new ArrayList<>(3);
    homePresenter.attachView(view);
    homePresenter.loadTopStories();
    verify(view).showLoading();
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    capturedCallback.onComplete(mockStories);
    verify(view).hideLoading();
    verify(view).showStatusText(R.string.loading_stories);
  }

  @Test public void clickOnItem_doMoveToDetailStory() throws Exception {
    Story story = new Story(1, "hello",0,  "bye",0);
    homePresenter.attachView(view);
    homePresenter.loadTopStories();
    homePresenter.onStoryClicked(story);
    verify(view).gotoStoryDetail(itemCaptor.capture());
    final Story actualStory = itemCaptor.getValue();
    assertTrue(story == actualStory);
  }

  @Test public void noInteractionWithViewOnLoadErrorAfterOnDetach() throws Exception {
    homePresenter.attachView(view);
    homePresenter.loadTopStories();
    verify(view).showLoading();
    int anyCode = 1;
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    homePresenter.detachView();
    callbackArgumentCaptor.getValue().onError(anyCode);
    verifyNoMoreInteractions(view);
  }

  @Test public void noInteractionWithViewOnLoadSuccessAfterOnDetach() throws Exception {
    homePresenter.attachView(view);
    homePresenter.loadTopStories();
    verify(view).showLoading();
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    homePresenter.detachView();
    callbackArgumentCaptor.getValue().onComplete(mockStories);
    verifyNoMoreInteractions(view);
  }

  @Test public void doRegister_OnResume(){
    homePresenter.attachView(view);
    homePresenter.onResume();
    verify(view).registerReceiver();
  }

  @Test public void doUnregister_OnPause(){
    homePresenter.attachView(view);
    homePresenter.onPause();
    verify(view).unregisterReceiver();
  }

  @Test public void onSwipeToRefresh_doCallRefresh() throws Exception {
    homePresenter.attachView(view);
    homePresenter.onSwipeToRefresh();
    verify(view).refreshTopStories();
  }
}
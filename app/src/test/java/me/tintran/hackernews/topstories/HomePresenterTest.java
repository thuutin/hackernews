package me.tintran.hackernews.topstories;

import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.Item;
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
  final List<Item> mockItems = new ArrayList<>(3);

  HomePresenter homePresenter;
  @Mock HomeContract.View view;
  @Mock TopStoriesUseCase topStoriesUseCase;

  @Captor ArgumentCaptor<TopStoriesUseCase.Callback> callbackArgumentCaptor;
  @Captor ArgumentCaptor<List<Item>> listArgumentCaptor;
  @Captor ArgumentCaptor<Item> itemCaptor;
  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    homePresenter = new HomePresenter(topStoriesUseCase);
    mockItems.add(new Item(1, "item1", "subtitle 1"));
    mockItems.add(new Item(2, "item2", "subtitle 2"));
    mockItems.add(new Item(3, "item3", "subtitle 3"));
  }

  @Test public void doShowItemsOnLoadError() throws Exception {
    homePresenter.attachView(view);
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    int fakeErrorCode = 1001;
    capturedCallback.onError(fakeErrorCode);
    verify(view).showStatusText(R.string.error_load_stories);
  }

  @Test public void doShowItemsOnLoadSuccess() throws Exception {
    homePresenter.attachView(view);
    verify(view).showStatusText(R.string.loading);
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    capturedCallback.onComplete(mockItems);
    verify(view).showItem(listArgumentCaptor.capture());
    List<Item> capturedListItem = listArgumentCaptor.getValue();
    assertEquals(capturedListItem.size(), mockItems.size());
    for (int i = 0; i < capturedListItem.size(); i++) {
      Item expectedItem = mockItems.get(i);
      Item actualItem = capturedListItem.get(i);
      assertTrue(expectedItem == actualItem);
      assertEquals(expectedItem.title, actualItem.title);
    }
  }

  @Test public void doShowItemsOnLoadSuccessWithEmptyList() throws Exception {
    final List<Item> mockItems = new ArrayList<>(3);
    homePresenter.attachView(view);
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    capturedCallback.onComplete(mockItems);
    verify(view).showStatusText(R.string.no_stories);
  }

  @Test public void clickOnItem_doMoveToDetailStory() throws Exception {
    Item item = new Item(id, "hello", "bye");
    homePresenter.attachView(view);
    homePresenter.onStoryClicked(item);
    verify(view).gotoStoryDetail(itemCaptor.capture());
    final Item actualItem = itemCaptor.getValue();
    assertTrue(item == actualItem);
  }

  @Test public void noInteractionWithViewOnLoadErrorAfterOnDetach() throws Exception {
    homePresenter.attachView(view);
    verify(view).showStatusText(R.string.loading);
    int anyCode = 1;
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    homePresenter.detachView();
    callbackArgumentCaptor.getValue().onError(anyCode);
    verifyNoMoreInteractions(view);
  }

  @Test public void noInteractionWithViewOnLoadSuccessAfterOnDetach() throws Exception {
    homePresenter.attachView(view);
    verify(view).showStatusText(R.string.loading);
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    homePresenter.detachView();
    callbackArgumentCaptor.getValue().onComplete(mockItems);
    verifyNoMoreInteractions(view);
  }
}
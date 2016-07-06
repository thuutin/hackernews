package me.tintran.hackernews.topstories;

import java.util.ArrayList;
import java.util.List;
import me.tintran.hackernews.R;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by tin on 7/6/16.
 */
public class HomePresenterTest {

  HomePresenter homePresenter;
  @Mock HomeContract.View view;
  @Mock TopStoriesUseCase topStoriesUseCase;

  @Captor ArgumentCaptor<TopStoriesUseCase.Callback> callbackArgumentCaptor;
  @Captor ArgumentCaptor<List<Item>> listArgumentCaptor;
  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    homePresenter = new HomePresenter(topStoriesUseCase);
  }

  @Test public void doShowItemsOnLoadError() throws Exception {
    homePresenter.attachView(view);
    verify(topStoriesUseCase).getTopStories(callbackArgumentCaptor.capture());
    final TopStoriesUseCase.Callback capturedCallback = callbackArgumentCaptor.getValue();
    int fakeErrorCode = 1001;
    capturedCallback.onError(fakeErrorCode);
    verify(view).showStatusText(R.string.no_stories);
  }

  @Test public void doShowItemsOnLoadSuccess() throws Exception {

    final List<Item> mockItems = new ArrayList<>(3);
    mockItems.add(new Item("item1"));
    mockItems.add(new Item("item2"));
    mockItems.add(new Item("item3"));

    homePresenter.attachView(view);
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

}
package me.tintran.hackernews.sync;

import java.util.List;
import me.tintran.hackernews.StoryCommentGateway;
import me.tintran.hackernews.StoryGateway;
import me.tintran.hackernews.TopStoryGateway;
import me.tintran.hackernews.data.HackerNewsApi;
import me.tintran.hackernews.sync.StoryApiHelper;
import me.tintran.hackernews.sync.TopStoriesCallback;
import me.tintran.hackernews.sync.TopStoryProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by tin on 7/10/16.
 */
public class TopStoryProcessorTest {

  TopStoryProcessor topStoryProcessor;

  @Mock StoryGateway storyGateway;

  @Mock TopStoryGateway topStoryGateway;
  @Mock StoryCommentGateway storyCommentGateway;
  @Mock TopStoryProcessor.Callback callback;
  @Mock StoryApiHelper storyApiHelper;
  @Mock Call<int[]> call;
  @Mock Call<int[]> call2;
  @Captor ArgumentCaptor<TopStoriesCallback> storyCallbackCaptor;
  @Captor ArgumentCaptor<Integer> intCaptor;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    topStoryProcessor =
        new TopStoryProcessor(storyApiHelper, storyGateway, topStoryGateway, storyCommentGateway,
            callback);
  }

  @Test public void getNullTopStories_doCallOnComplete() throws Exception {
    when(storyApiHelper.getTopStoryIds()).thenReturn(null);
    topStoryProcessor.execute();
    verify(callback).onComplete();
    verifyNoMoreInteractions(topStoryGateway);
    verifyNoMoreInteractions(storyGateway);
    verifyNoMoreInteractions(storyCommentGateway);
  }

  @Test public void getNullUpdatedStories_doCallOnComplete() throws Exception {
    int[] value = new int[2];
    when(storyApiHelper.getTopStoryIds()).thenReturn(value);
    when(storyApiHelper.getUpdatedStoryIds()).thenReturn(null);
    topStoryProcessor.execute();
    verify(callback).onComplete();
    verify(topStoryGateway).replaceTopStoryIds(value);
    verifyNoMoreInteractions(storyGateway);
    verifyNoMoreInteractions(storyCommentGateway);
  }

  @Test public void throwIOExceptionWhenGetUpdatedStories_doCallOnComplete() throws Exception {
    TopStoryProcessor spy = Mockito.spy(topStoryProcessor);
    int[] topStories = new int[2];
    when(storyApiHelper.getTopStoryIds()).thenReturn(topStories);
    when(storyApiHelper.getUpdatedStoryIds()).thenReturn(new int[2]);
    when(storyGateway.getLocalTopStoryIds()).thenReturn(new int[2]);
    when(spy.getIdsToRetrieve(any(int[].class), any(int[].class), any(int[].class))).thenReturn(
        new int[] { 1, 3, 5 });
    doCallRealMethod().when(spy).execute();
    spy.execute();
    verify(storyApiHelper, times(3)).getStory(anyInt(), storyCallbackCaptor.capture());
    HackerNewsApi.StoryItem storyItem1 = new HackerNewsApi.StoryItem();
    storyItem1.id = 1;
    HackerNewsApi.StoryItem storyItem3 = new HackerNewsApi.StoryItem();
    storyItem3.id = 3;
    HackerNewsApi.StoryItem storyItem5 = new HackerNewsApi.StoryItem();
    storyItem5.id = 5;

    List<TopStoriesCallback> allValues = storyCallbackCaptor.getAllValues();
    allValues.get(0).onResponse(null, Response.success(storyItem1));
    allValues.get(1).onResponse(null, Response.success(storyItem3));
    allValues.get(2).onResponse(null, Response.success(storyItem5));
    verify(topStoryGateway).replaceTopStoryIds(topStories);
    verify(storyGateway, times(3)).insertStory(intCaptor.capture(), isNull(String.class), anyInt(), anyBoolean(), anyInt(), anyLong(), isNull(String.class), isNull(String.class));
    assertEquals(1, ((int) intCaptor.getAllValues().get(0)));
    assertEquals(3, ((int) intCaptor.getAllValues().get(1)));
    assertEquals(5, ((int) intCaptor.getAllValues().get(2)));
  }
}
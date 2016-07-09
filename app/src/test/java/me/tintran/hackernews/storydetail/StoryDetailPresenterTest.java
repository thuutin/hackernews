package me.tintran.hackernews.storydetail;

import java.util.Arrays;
import java.util.List;
import me.tintran.hackernews.R;
import me.tintran.hackernews.data.CommentListUseCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by tin on 7/7/16.
 */
public class StoryDetailPresenterTest {

  @Mock StoryDetailContract.View view;
  @Mock CommentListUseCase commentListUseCase;
  @Captor ArgumentCaptor<List<Comment>> listArgumentCaptor;
  @Captor ArgumentCaptor<CommentListUseCase.Callback> callbackArgumentCaptor;

  StoryDetailContract.ActionsListener actionsListener;
  private int storyId;

  private List<Comment> mockCommentList;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    storyId = 1;
    mockCommentList = Arrays.asList(
        new Comment(1, "comment 1"),
        new Comment(2, "comment 2"),
        new Comment(3, "comment 3")
    );
    actionsListener = new StoryDetailPresenter(storyId, commentListUseCase);
  }

  @Test public void doShowListCommentsOnLoadSuccess() throws Exception {
    actionsListener.attachView(view);
    verify(view).showStatusText(R.string.loading);
    verify(commentListUseCase).getCommentList(eq(storyId), callbackArgumentCaptor.capture());
    callbackArgumentCaptor.getValue().onComplete(mockCommentList);
    verify(view).showCommentList(listArgumentCaptor.capture());
    List<Comment> comments = listArgumentCaptor.getValue();
    assertEquals(comments.size(), mockCommentList.size());
    for (int i = 0; i < comments.size(); i++) {
      final Comment expected = mockCommentList.get(i);
      final Comment actual = comments.get(i);
      assertEquals(actual.text, expected.text);
      assertEquals(actual.id, expected.id);
    }
  }
}
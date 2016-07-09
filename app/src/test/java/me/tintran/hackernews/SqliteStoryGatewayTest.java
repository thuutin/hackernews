package me.tintran.hackernews;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import me.tintran.hackernews.data.StoryContract;
import me.tintran.hackernews.data.StoryContract.StoryColumns;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;

/**
 * Created by tin on 7/9/16.
 */
public class SqliteStoryGatewayTest {

  @Mock SQLiteDatabase sqLiteDatabase;
  @Captor ArgumentCaptor<ContentValues> contentValuesArgumentCaptor;

  StoryGateway.SqliteStoryGateway sqliteStoryGateway;

  @Before public void setUp() throws Exception {

    MockitoAnnotations.initMocks(this);
    sqliteStoryGateway = new StoryGateway.SqliteStoryGateway(sqLiteDatabase);
  }

  @Test public void insertStory() throws Exception {
    //String title = "title fake";
    //int id = 88;
    //int decendants = 9;
    //int score = 18;
    //String type = "type";
    //String url1 = "url1";
    //int time = 1;
    //sqliteStoryGateway.insertStory(id, title, decendants, score, time, type, url1);
    //
    //Mockito.verify(sqLiteDatabase)
    //    .insertWithOnConflict(eq(StoryColumns.TABLE_NAME), isNull(String.class),
    //        contentValuesArgumentCaptor.capture(), anyInt());
    //ContentValues value = contentValuesArgumentCaptor.getValue();
    //assertEquals(type, value.get(StoryColumns.COLUMN_NAME_TYPE));
    //assertEquals(time, value.get(StoryColumns.COLUMN_NAME_TIME));
    //assertEquals(decendants, value.get(StoryColumns.COLUMN_NAME_DESCENDANTS));
    //assertEquals(score, value.get(StoryColumns.COLUMN_NAME_SCORE));
    //assertEquals(id, value.get(StoryColumns._ID));
    //assertEquals(url1, value.get(StoryColumns.COLUMN_NAME_URL));
  }
}
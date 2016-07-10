package me.tintran.hackernews.sync;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tin on 7/10/16.
 */
public class StoryHelper {

  private final int[] topStoryIds;
  private final int[] updatedIds;
  private final int[] storyIdsFromDatabase;

  public StoryHelper(int[] topStoryIds, int[] updatedIds, int[] storyIdsFromDatabase) {
    this.topStoryIds = topStoryIds;
    this.updatedIds = updatedIds;
    this.storyIdsFromDatabase = storyIdsFromDatabase;
  }

  public int[] getIdsToRetrieve(){
    int[] intersection = getIntersection(updatedIds, topStoryIds);
    // retrieve everything in the intersection and also story item that is not in the database
    return topStoryIds;
  }

  private static int[] getIntersection(int[] updatedStories, int[] topStoryIds) {
    List<Integer> integers = new ArrayList<>();
    for (int updatedStory : updatedStories) {
      for (int topStoryId : topStoryIds) {
        if (topStoryId == updatedStory) {
          integers.add(updatedStory);
        }
      }
    }
    int[] results = new int[integers.size()];
    for (int i = 0; i < integers.size(); i++) {
      results[i] = integers.get(i);
    }
    return results;
  }
}

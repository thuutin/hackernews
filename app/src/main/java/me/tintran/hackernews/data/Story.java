package me.tintran.hackernews.data;

/**
 * Created by tin on 7/6/16.
 */
public final class Story {
  public final int id;
  public final String title;
  public final int score;
  public final int time;

  public Story(int id, String title, int score, int time) {
    this.id = id;
    this.title = title;
    this.score = score;
    this.time = time;
  }
}

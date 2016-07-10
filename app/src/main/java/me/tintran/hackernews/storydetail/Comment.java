package me.tintran.hackernews.storydetail;

/**
 * Created by tin on 7/7/16.
 */
public final class Comment {
  public final int id;
  public final String text;
  public final String by;
  public int time;

  public Comment(int id, String text, String by, int time) {
    this.id = id;
    this.text = text;
    this.by = by;
    this.time = time;
  }
}

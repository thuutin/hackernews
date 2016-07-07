package me.tintran.hackernews.data;

/**
 * Created by tin on 7/6/16.
 */
public final class Story {
  public final int id;
  public final String title;
  public final String subtitle;

  public Story(int id, String title, String subtitle) {
    this.id = id;
    this.title = title;
    this.subtitle = subtitle;
  }
}

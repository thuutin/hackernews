package me.tintran.hackernews.data;

/**
 * Created by tin on 7/6/16.
 */
public final class Item {
  public final int id;
  public final String title;
  public final String subtitle;

  public Item(int id, String title, String subtitle) {
    this.id = id;
    this.title = title;
    this.subtitle = subtitle;
  }
}

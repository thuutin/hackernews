package me.tintran.hackernews.storydetail;

import android.text.Html;
import android.text.Spanned;

/**
 * Created by tin on 7/7/16.
 */
public final class Comment {
  public final int id;
  public final String text;

  public Comment(int id, String text) {
    this.id = id;
    this.text = text;
  }
}

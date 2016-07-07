package me.tintran.hackernews.topstories;

import android.support.annotation.StringRes;
import java.util.List;
import me.tintran.hackernews.data.Item;

/**
 * Created by tin on 7/6/16.
 */

interface HomeContract {

  interface View {

    void hideStatusText();

    void showStatusText(@StringRes int statusRes);

    void showItem(List<Item> itemsList);

    void gotoStoryDetail(Item item);
  }

  interface ActionsListener {


    void detachView();

    void attachView(HomeContract.View view);

    void onStoryClicked(Item item);
  }

}

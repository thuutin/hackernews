package me.tintran.hackernews.topstories;

import android.support.annotation.StringRes;
import java.util.List;

/**
 * Created by tin on 7/6/16.
 */

interface HomeContract {

  interface View {

    void hideStatusText();

    void showStatusText(@StringRes int statusRes);

    void showItem(List<Item> itemsList);
  }

  interface ActionsListener {


    void detachView();

    void attachView(HomeContract.View view);
  }

}

package me.tintran.hackernews.topstories;

import android.support.annotation.StringRes;
import java.util.List;
import me.tintran.hackernews.data.Story;

/**
 * Created by tin on 7/6/16.
 */

interface HomeContract {

  interface View {

    void hideStatusText();

    void showStatusText(@StringRes int statusRes);

    void showItem(List<Story> itemsList);

    void gotoStoryDetail(Story story);
  }

  interface ActionsListener {


    void detachView();

    void attachView(HomeContract.View view);

    void onStoryClicked(Story story);
  }

}

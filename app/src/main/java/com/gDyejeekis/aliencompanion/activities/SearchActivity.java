package com.gDyejeekis.aliencompanion.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SearchActivity extends SwipeBackActivity {

    public static boolean isForeground;
    private SearchFragment searchFragment;
    private PostFragment postFragment;
    private FragmentManager fm;
    private FrameLayout container;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_scrollable);
        initToolbar();
        container = (FrameLayout) findViewById(R.id.container_main);

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);

        fm = getSupportFragmentManager();

        int resource;
        if(MyApplication.dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MyApplication.dualPaneActive = true;
            View.inflate(this, R.layout.activity_dual_pane, container);
            resource = R.id.listFragmentHolder;
        }
        else {
            MyApplication.dualPaneActive = false;
            View.inflate(this, R.layout.activity_single_pane, container);
            resource = R.id.fragmentHolder;
        }

        setupMainFragment(resource);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
    }

    public void setupPostFragment(PostFragment postFragment) {
        this.postFragment = postFragment;
        fm.beginTransaction().replace(R.id.postFragmentHolder, postFragment, "postFragment").commit();
    }

    private void setupMainFragment(int container) {
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentHolder);
        if(searchFragment == null) {
            searchFragment = new SearchFragment();
            getSupportFragmentManager().beginTransaction().add(container, searchFragment, "listFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        if(MyApplication.dualPaneActive) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    MyApplication.actionSort = true;
                    showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MyApplication.actionSort = false;
                    showPostsOrCommentsPopup(findViewById(R.id.action_refresh));
                    return true;
            }
        }
        if(item.getItemId() == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPostsOrCommentsPopup(final View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_posts_or_comments);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_posts:
                        if (MyApplication.actionSort) searchFragment.showSortPopup(v);
                        else searchFragment.refreshList();
                        return true;
                    case R.id.action_comments:
                        PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                        if (postFragment != null) {
                            if (MyApplication.actionSort) postFragment.showSortPopup(v);
                            else postFragment.refreshPostAndComments();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    //@Override
    //public void onDestroy() {
    //    super.onDestroy();
    //}

    public SearchFragment getSearchFragment() {
        return searchFragment;
    }

    public PostFragment getPostFragment() {
        return postFragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(MyApplication.dualPane) {
            container.removeAllViews();
            fm.beginTransaction().remove(searchFragment).commitAllowingStateLoss();
            searchFragment = recreateSearchFragment(searchFragment);
            int resource;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MyApplication.dualPaneActive = true;
                View.inflate(this, R.layout.activity_dual_pane, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = MainActivity.recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            } else {
                MyApplication.dualPaneActive = false;
                View.inflate(this, R.layout.activity_single_pane, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, searchFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private SearchFragment recreateSearchFragment(SearchFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        SearchFragment newInstance = SearchFragment.newInstance(f.adapter, f.searchQuery, f.searchSort, f.timeSpan, f.hasMore, f.currentLoadType);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

}

package com.gDyejeekis.aliencompanion.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.VerifyAccountDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMultis;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMutliredditItem;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.enums.MenuType;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerHeader;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubredditItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubreddits;
import com.gDyejeekis.aliencompanion.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.imid.swipebacklayout.lib.ViewDragHelper;


public class MainActivity extends AppCompatActivity {

    private FragmentManager fm;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private PostListFragment listFragment;
    //private PostFragment postFragment;
    private RecyclerView drawerContent;
    private NavDrawerAdapter adapter;
    private DrawerLayout.LayoutParams drawerParams;
    private NavigationView scrimInsetsFrameLayout;
    private Toolbar toolbar;
    private LinearLayout container;

    public static boolean dualPaneActive;

    public static boolean setupAccount = false;
    public static String oauthCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOrientation();
        MyApplication.setThemeRelatedFields();
        getTheme().applyStyle(MyApplication.fontStyle, true);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else {
            getTheme().applyStyle(R.style.selectedTheme_day, true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_plus);

        container = (LinearLayout) findViewById(R.id.container);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);
            toolbar.setPopupTheme(R.style.OverflowStyleDark);
        }
        toolbar.setBackgroundColor(MyApplication.currentColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fm = getFragmentManager();

        initNavDrawer();

        int resource;
        if(MyApplication.dualPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            dualPaneActive = true;
            View.inflate(this, R.layout.activity_main_dual_panel, container);
            resource = R.id.listFragmentHolder;
        }
        else {
            View.inflate(this, R.layout.activity_main, container);
            dualPaneActive = false;
            resource = R.id.fragmentHolder;
        }
        setupMainFragment(resource);
    }

    private void setOrientation() {
        MyApplication.currentOrientation = MyApplication.screenOrientation;
        switch (MyApplication.screenOrientation) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
        }
    }

    public void setupPostFragment(PostFragment postFragment) {
        PostFragment oldFragment = (PostFragment) fm.findFragmentByTag("postFragment");
        if(oldFragment!=null) fm.beginTransaction().remove(oldFragment).commit();
        fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commit();
    }

    private void setupMainFragment(int containerRes) {
        listFragment = (PostListFragment) fm.findFragmentById(containerRes);
        if(listFragment == null) {
            listFragment = new PostListFragment();
            fm.beginTransaction().add(containerRes, listFragment, "listFragment").commit();
        }
    }

    public void changeCurrentUser(SavedAccount account) {
        MyApplication.currentAccount = account;
        //MyApplication.currentUser = (account.loggedIn) ? new User(null, account.getUsername(), account.getModhash(), account.getCookie()) : null;
        if(account.loggedIn) {
            if(account.oauth2) {
                MyApplication.currentUser = new User(null, account.getUsername(), account.getToken());
                MyApplication.currentAccessToken = account.getToken().accessToken;
            }
            else {
                MyApplication.currentUser = new User(null, account.getUsername(), account.getModhash(), account.getCookie());
                MyApplication.currentAccessToken = null;
            }
        }
        else {
            MyApplication.currentUser = null;
            try {
                MyApplication.currentAccessToken = account.getToken().accessToken;
            } catch (NullPointerException e) {
                MyApplication.currentAccessToken = null;
            }
        }

        if(MyApplication.currentUser!=null) {
            adapter.showUserMenuItems();
        }
        else {
            adapter.hideUserMenuItems();
        }
        adapter.updateSubredditItems(MyApplication.currentAccount.getSubreddits());
        adapter.updateMultiredditItems(MyApplication.currentAccount.getMultireddits());
        //homePage();
        try {
            listFragment.changeSubreddit(listFragment.subreddit);
            String message = (MyApplication.currentUser==null) ? "Logged out" : "Logged in as " + account.getUsername();
            ToastUtils.displayShortToast(this, message);
        } catch (NullPointerException e) {}
    }

    public void homePage() {
        if(listFragment!=null) listFragment.changeSubreddit(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        listFragment.loadMore = MyApplication.endlessPosts;

        if(setupAccount) {
            setupAccount = false;

            VerifyAccountDialogFragment dialog = new VerifyAccountDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("code", oauthCode);
            dialog.setArguments(bundle);
            dialog.show(getFragmentManager(), "dialog");

            oauthCode = null;
        }

        if(EditSubredditsActivity.changesMade) {
            EditSubredditsActivity.changesMade = false;
            getNavDrawerAdapter().updateSubredditItems(MyApplication.currentAccount.getSubreddits());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<SavedAccount> accounts = new ArrayList<SavedAccount>();
                    boolean check = true;
                    //if(currentAccount != null) {
                        for (NavDrawerAccount accountItem : adapter.accountItems) {
                            if (accountItem.getAccountType() == NavDrawerAccount.TYPE_ACCOUNT || accountItem.getAccountType() == NavDrawerAccount.TYPE_LOGGED_OUT) {
                                SavedAccount accountToSave;
                                if (check && accountItem.getName().equals(MyApplication.currentAccount.getUsername())) {
                                    check = false;
                                    accountToSave = MyApplication.currentAccount;
                                } else accountToSave = accountItem.savedAccount;
                                accounts.add(accountToSave);
                            }
                        }
                        getNavDrawerAdapter().saveAccounts(accounts);
                    //}
                }
            });
        }

        //TODO: check and apply multireddit list changes

        if(MyApplication.currentOrientation != MyApplication.screenOrientation) setOrientation();

        if(MyApplication.dualPaneCurrent != MyApplication.dualPane) {
            MyApplication.dualPaneCurrent = MyApplication.dualPane;
            toggleDualPane();
        }

        if(MyApplication.currentFontStyle != MyApplication.fontStyle) {
            MyApplication.currentFontStyle = MyApplication.fontStyle;
            getTheme().applyStyle(MyApplication.fontStyle, true);
            listFragment.redrawList();
        }

        if(!MyApplication.nightThemeEnabled && MyApplication.currentColor != MyApplication.colorPrimary) {
            MyApplication.currentColor = MyApplication.colorPrimary;
            MyApplication.linkColor = MyApplication.colorPrimary;
            toolbar.setBackgroundColor(MyApplication.colorPrimary);
            MyApplication.colorPrimaryDark = MyApplication.getPrimaryDarkColor(MyApplication.primaryColors, MyApplication.primaryDarkColors);
            drawerLayout.setStatusBarBackgroundColor(MyApplication.colorPrimaryDark);
            listFragment.colorSchemeChanged();
            adapter.notifyDataSetChanged();
        }

        if(MyApplication.drawerGravity != drawerParams.gravity) {
            drawerParams.gravity = MyApplication.drawerGravity;
            scrimInsetsFrameLayout.setLayoutParams(drawerParams);
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public boolean onOptionsItemSelected(MenuItem item) {
                    if (item != null && item.getItemId() == android.R.id.home) {
                        if (drawerLayout.isDrawerOpen(MyApplication.drawerGravity)) {
                            drawerLayout.closeDrawer(MyApplication.drawerGravity);
                        } else {
                            drawerLayout.openDrawer(MyApplication.drawerGravity);
                        }
                    }
                    return false;
                }
            };
            drawerLayout.setDrawerListener(drawerToggle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuResource;
        if(MyApplication.offlineModeEnabled) menuResource = R.menu.menu_main_offline;
        else menuResource = R.menu.menu_main;
        getMenuInflater().inflate(menuResource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(dualPaneActive) {
            switch (item.getItemId()) {
                case R.id.action_sort:
                    MyApplication.actionSort = true;
                    showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    return true;
                case R.id.action_refresh:
                    MyApplication.actionSort = false;
                    try {
                        showPostsOrCommentsPopup(findViewById(R.id.action_refresh));
                    } catch (Exception e) {
                        showPostsOrCommentsPopup(findViewById(R.id.action_sort));
                    } //TODO: find a more suitable anchor
                    return true;
            }
        }
        return drawerToggle.onOptionsItemSelected(item);
    }

    private void showPostsOrCommentsPopup(final View v) {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_posts_or_comments);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_posts:
                        if (MyApplication.actionSort) listFragment.showSortPopup(v);
                        else listFragment.refreshList();
                        return true;
                    case R.id.action_comments:
                        PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                        if (postFragment != null) {
                            if (MyApplication.actionSort) postFragment.showSortPopup(v);
                            else postFragment.refreshComments();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

        if(MyApplication.dualPane) {
            container.removeViewAt(1);
            fm.beginTransaction().remove(listFragment).commitAllowingStateLoss();
            listFragment = recreateListFragment(listFragment);
            int resource;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                dualPaneActive = true;
                View.inflate(this, R.layout.activity_main_dual_panel, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            } else {
                dualPaneActive = false;
                View.inflate(this, R.layout.activity_main, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, listFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private void toggleDualPane() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            container.removeViewAt(1);
            fm.beginTransaction().remove(listFragment).commitAllowingStateLoss();
            listFragment = recreateListFragment(listFragment);
            int resource;
            if(MyApplication.dualPane) {
                dualPaneActive = true;
                View.inflate(this, R.layout.activity_main_dual_panel, container);
                resource = R.id.listFragmentHolder;

                PostFragment postFragment = (PostFragment) fm.findFragmentByTag("postFragment");
                if(postFragment!=null) {
                    fm.beginTransaction().remove(postFragment).commitAllowingStateLoss();
                    postFragment = recreatePostFragment(postFragment, fm);
                    fm.beginTransaction().add(R.id.postFragmentHolder, postFragment, "postFragment").commitAllowingStateLoss();
                }
            }
            else {
                dualPaneActive = false;
                View.inflate(this, R.layout.activity_main, container);
                resource = R.id.fragmentHolder;
            }
            fm.beginTransaction().add(resource, listFragment, "listFragment").commitAllowingStateLoss();
        }
    }

    private void initNavDrawer() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        scrimInsetsFrameLayout = (NavigationView) findViewById(R.id.scrimInsetsFrameLayout);
        drawerLayout.setStatusBarBackgroundColor(MyApplication.colorPrimaryDark);

        drawerParams = new DrawerLayout.LayoutParams(calculateDrawerWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        final int gravity = (MyApplication.prefs.getString("navDrawerSide", "Left").equals("Left")) ? Gravity.LEFT : Gravity.RIGHT;
        drawerParams.gravity = gravity;
        scrimInsetsFrameLayout.setLayoutParams(drawerParams);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                if (item != null && item.getItemId() == android.R.id.home) {
                    if (drawerLayout.isDrawerOpen(gravity)) {
                        drawerLayout.closeDrawer(gravity);
                    } else {
                        drawerLayout.openDrawer(gravity);
                    }
                }
                return false;
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        initNavDrawerContent();
    }

    public void initNavDrawerContent() {
        drawerContent = (RecyclerView) findViewById((R.id.drawer_content));
        drawerContent.setLayoutManager(new LinearLayoutManager(this));
        drawerContent.setHasFixedSize(true);

        adapter = new NavDrawerAdapter(this);
        adapter.add(new NavDrawerHeader());
        adapter.addAll(getMenuItems());
        adapter.add(new NavDrawerSubreddits());
        adapter.addAll(getDefaultSubredditItems());
        adapter.add(new NavDrawerMultis());

        drawerContent.setAdapter(adapter);

        adapter.importAccounts();

        //adapter.add(new NavDrawerMutliredditItem("multi 1"));
        //adapter.add(new NavDrawerMutliredditItem("multi 2"));
        //adapter.add(new NavDrawerMutliredditItem("multi 3"));
        //adapter.add(new NavDrawerMutliredditItem("multi 4"));
    }

    private List<NavDrawerItem> getMenuItems() {
        List<NavDrawerItem> menuItems = new ArrayList<>();
        menuItems.add(new NavDrawerMenuItem(MenuType.user));
        menuItems.add(new NavDrawerMenuItem(MenuType.subreddit));
        menuItems.add(new NavDrawerMenuItem(MenuType.settings));

        return menuItems;
    }

    private List<NavDrawerItem> getDefaultSubredditItems() {
        List<NavDrawerItem> subredditItems = new ArrayList<>();
        subredditItems.add(new NavDrawerSubredditItem());
        for (String subreddit : MyApplication.defaultSubredditStrings) {
            subredditItems.add(new NavDrawerSubredditItem(subreddit));
        }

        return subredditItems;
    }

    private int calculateDrawerWidth() {
        final float scale = getResources().getDisplayMetrics().density;
        int drawerWidth = (int) (320 * scale + 0.5f);

        return drawerWidth;
        //return GeneralUtils.getPortraitWidth(this)/2;
    }

    public PostListFragment getListFragment() {
        return listFragment;
    }

    public RecyclerView getNavDrawerView() {
        return drawerContent;
    }

    public NavDrawerAdapter getNavDrawerAdapter() {
        return adapter;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT) || drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawers();
        }
        else {
            //MyApplication.currentUser = null; //user connects every time main activity is started - RE-ENABLE THIS IF NEEDED
            super.onBackPressed();
        }
    }

    private PostListFragment recreateListFragment(PostListFragment f) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostListFragment newInstance = PostListFragment.newInstance(f.postListAdapter, f.subreddit, f.submissionSort, f.timeSpan, f.currentLoadType, f.hasMore);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

    public static PostFragment recreatePostFragment(PostFragment f, FragmentManager fm) {
        Fragment.SavedState savedState = fm.saveFragmentInstanceState(f);

        PostFragment newInstance = PostFragment.newInstance(f.postAdapter);
        newInstance.setInitialSavedState(savedState);

        return newInstance;
    }

}

package com.gDyejeekis.aliencompanion.fragments;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.broadcast_receivers.RedditItemSubmittedReceiver;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserContentTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends RedditContentFragment {

    public static final String TAG = "UserFragment";

    public String username;
    public UserOverviewSort userOverviewSort;
    public UserSubmissionsCategory userContent, tempCategory;
    public LoadUserContentTask task;
    private BroadcastReceiver commentEditReceiver;

    public static UserFragment newInstance(RedditItemListAdapter adapter, String username, UserOverviewSort sort, UserSubmissionsCategory category, boolean hasMore) {
        UserFragment newInstance = new UserFragment();
        newInstance.adapter = adapter;
        newInstance.username = username;
        newInstance.userOverviewSort = sort;
        newInstance.userContent = category;
        newInstance.hasMore = hasMore;
        return newInstance;
    }

    @Override
    public boolean hasFabNavigation() {
        return false;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if(bundle!=null) {
            username = bundle.getString("username");
            userOverviewSort = (UserOverviewSort) bundle.getSerializable("sort");
            userContent = (UserSubmissionsCategory) bundle.getSerializable("category");
        }

        if(username==null) {
            username = activity.getIntent().getStringExtra("username");
            userOverviewSort = (UserOverviewSort) activity.getIntent().getSerializableExtra("sort");
            userContent = (UserSubmissionsCategory) activity.getIntent().getSerializableExtra("category");
        }

        if(activity instanceof UserActivity && userContent == UserSubmissionsCategory.SAVED) {
            ((UserActivity) activity).setAddToSyncedVisible(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("username", username);
        outState.putSerializable("sort", userOverviewSort);
        outState.putSerializable("category", userContent);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(adapter!=null && !MyApplication.dualPane && userContent == UserSubmissionsCategory.OVERVIEW)
            adapter.notifyItemChanged(0);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        if(userContent == null) {
            userContent = UserSubmissionsCategory.OVERVIEW;
            userOverviewSort = UserOverviewSort.NEW;
        }
        setActionBarSubtitle();
        registerReceivers();
    }

    @Override
    public void onDetach() {
        unregisterReceivers();
        super.onDetach();
    }

    private void registerReceivers() {
        commentEditReceiver = new RedditItemSubmittedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RedditItemSubmittedReceiver.COMMENT_EDIT);
        activity.registerReceiver(commentEditReceiver, filter);
    }

    private void unregisterReceivers() {
        activity.unregisterReceiver(commentEditReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reddit_content, container, false);
        contentView = view.findViewById(R.id.recyclerView_postList);
        contentView.addOnScrollListener(onScrollListener);
        updateContentViewProperties();
        updateCurrentViewType();
        initMainProgressBar(view);
        initSwipeRefreshLayout(view);
        initFabNavOptions();

        if(currentLoadType == null) {
            if (adapter == null) {
                currentLoadType = LoadType.init;
                if(userContent==null) {
                    userContent = UserSubmissionsCategory.OVERVIEW;
                    userOverviewSort = UserOverviewSort.NEW;
                }
                task = new LoadUserContentTask(activity, this, LoadType.init);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
            }
        }
        else switch (currentLoadType) {
            case init:
                contentView.setVisibility(View.GONE);
                mainProgressBar.setVisibility(View.VISIBLE);
                break;
            case refresh:
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                adapter.setLoadingMoreItems(true);
                break;
        }

        return view;
    }

    @Override
    public void refreshList() {
        dismissSnackbar();
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadUserContentTask(activity, this, LoadType.refresh);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void extendList() {
        dismissSnackbar();
        currentLoadType = LoadType.extend;
        adapter.setLoadingMoreItems(true);
        task = new LoadUserContentTask(activity, this, LoadType.extend);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshUser(UserSubmissionsCategory category, UserOverviewSort sort) {
        dismissSnackbar();
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadUserContentTask(activity, this, LoadType.refresh, category, sort);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void changeUser(String username) {
        dismissSnackbar();
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.init;
        this.username = username;
        this.userContent = UserSubmissionsCategory.OVERVIEW;
        this.userOverviewSort = UserOverviewSort.NEW;
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        task = new LoadUserContentTask(activity, this, LoadType.init);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void itemEdited(int position, RedditItem item) {
        try {
            adapter.updateItem(position, item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(username);
    }

    public void setActionBarSubtitle() {
        String subtitle = userContent.value();
        if(userOverviewSort != null) subtitle = subtitle.concat(": " + userOverviewSort.value());
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                refreshList();
                return true;
            case R.id.action_sort:
                showContentPopup(activity.findViewById(R.id.action_sort));
                return true;
            case R.id.action_add_to_synced:
                showCustomSyncDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addSavedToSyncQueue(SyncProfileOptions syncOptions) {
        String toastMessage;
        if (GeneralUtils.isNetworkAvailable(activity)) {
            boolean syncOverWifiOnly = (syncOptions==null) ? MyApplication.syncOverWifiOnly : syncOptions.isSyncOverWifiOnly();
            if (syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(activity)) {
                toastMessage = "Syncing over mobile data connection is disabled";
            } else {
                toastMessage = "Saved posts added to sync queue";
                Intent intent = new Intent(activity, DownloaderService.class);
                intent.putExtra("syncSaved", true);
                intent.putExtra("syncOptions", syncOptions);
                activity.startService(intent);
            }
        } else {
            toastMessage = "Network connection unavailable";
        }
        ToastUtils.showToast(activity, toastMessage);
    }

    public void showContentPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        if(MyApplication.currentUser!=null && username.equals(MyApplication.currentUser.getUsername())) {
            popupMenu.inflate(R.menu.menu_user_content_account);
        }
        else {
            popupMenu.inflate(R.menu.menu_user_content);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_user_overview:
                        tempCategory = UserSubmissionsCategory.OVERVIEW;
                        showSortPopup();
                        return true;
                    case R.id.action_user_comments:
                        tempCategory = UserSubmissionsCategory.COMMENTS;
                        showSortPopup();
                        return true;
                    case R.id.action_user_submitted:
                        tempCategory = UserSubmissionsCategory.SUBMITTED;
                        showSortPopup();
                        return true;
                    case R.id.action_user_gilded:
                        refreshUser(UserSubmissionsCategory.GILDED, null);
                        return true;
                    case R.id.action_user_upvoted:
                        refreshUser(UserSubmissionsCategory.LIKED, null);
                        return true;
                    case R.id.action_user_downvoted:
                        refreshUser(UserSubmissionsCategory.DISLIKED, null);
                        return true;
                    case R.id.action_user_hidden:
                        refreshUser(UserSubmissionsCategory.HIDDEN, null);
                        return true;
                    case R.id.action_user_saved:
                        refreshUser(UserSubmissionsCategory.SAVED, null);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortPopup() {
        showSortPopup(activity.findViewById(R.id.action_sort));
    }

    private void showSortPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        popupMenu.inflate(R.menu.menu_user_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_new:
                        refreshUser(tempCategory, UserOverviewSort.NEW);
                        return true;
                    case R.id.action_sort_hot:
                        refreshUser(tempCategory, UserOverviewSort.HOT);
                        return true;
                    case R.id.action_sort_top:
                        refreshUser(tempCategory, UserOverviewSort.TOP);
                        return true;
                    case R.id.action_sort_controversial:
                        refreshUser(tempCategory, UserOverviewSort.COMMENTS);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

}

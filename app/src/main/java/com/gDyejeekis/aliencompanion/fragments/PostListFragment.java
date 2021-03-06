package com.gDyejeekis.aliencompanion.fragments;


import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

//import com.crashlytics.android.Crashlytics;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.broadcast_receivers.RedditItemSubmittedReceiver;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.info_dialog_fragments.InfoDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments.SyncOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SearchRedditDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadPostListTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SubredditSidebarDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.enums.SubmitType;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostListFragment extends RedditContentFragment {

    public static final String TAG = "PostListFragment";

    public String subreddit;
    public boolean isMulti = false;
    public boolean isOther = false;
    public SubmissionSort submissionSort;
    private SubmissionSort tempSort;
    public TimeSpan timeSpan;
    public LoadPostListTask task;
    private BroadcastReceiver submittedReceiver;

    public static PostListFragment newInstance(RedditItemListAdapter adapter, String subreddit, boolean isMulti, SubmissionSort sort, TimeSpan time, LoadType currentLoadType, boolean hasMore) {
        PostListFragment listFragment = new PostListFragment();
        listFragment.adapter = adapter;
        listFragment.subreddit = subreddit;
        listFragment.isMulti = isMulti;
        listFragment.submissionSort = sort;
        listFragment.timeSpan = time;
        listFragment.currentLoadType = currentLoadType;
        listFragment.hasMore = hasMore;
        return listFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null) {
            subreddit = savedInstanceState.getString("subreddit");
            isMulti = savedInstanceState.getBoolean("isMulti");
            isOther = savedInstanceState.getBoolean("isOther");
            submissionSort = (SubmissionSort) savedInstanceState.getSerializable("sort");
            timeSpan = (TimeSpan) savedInstanceState.getSerializable("time");
        }

        if(subreddit==null) subreddit = activity.getIntent().getStringExtra("subreddit");
        if(!isMulti) isMulti = activity.getIntent().getBooleanExtra("isMulti", false);
        if(!isOther) isOther = activity.getIntent().getBooleanExtra("isOther", false);
        if(submissionSort==null) submissionSort = (SubmissionSort) activity.getIntent().getSerializableExtra("sort");
        if(timeSpan==null) timeSpan = (TimeSpan) activity.getIntent().getSerializableExtra("time");
    }

    @Override
    protected void showViewsPopup(View v) {
        super.showViewsPopup(v);

        // ask whether to remember subreddit specific views
        if(!MyApplication.askedRememberPostView) {
            MyApplication.askedRememberPostView = true;
            final SharedPreferences.Editor editor = MyApplication.prefs.edit();
            editor.putBoolean("askedRememberView", true);
            editor.apply();

            final String text = "Remember the last used view option for each subreddit/multireddit?";
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyApplication.rememberPostListView = true;
                    editor.putBoolean("rememberView", true);
                    editor.apply();
                }
            };
            new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.MyAlertDialogStyle)).setMessage(text).setPositiveButton("Remember", listener)
                    .setNegativeButton("Cancel", null).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("subreddit", subreddit);
        outState.putBoolean("isMulti", isMulti);
        outState.putBoolean("isOther", isOther);
        outState.putSerializable("sort", submissionSort);
        outState.putSerializable("time", timeSpan);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_reddit_content, container, false);
        contentView = view.findViewById(R.id.recyclerView_postList);
        contentView.addOnScrollListener(onScrollListener);
        updateContentViewProperties();
        updateCurrentViewType();
        initMainProgressBar(view);
        initSwipeRefreshLayout(view);
        initFabNavOptions();

        if(currentLoadType == null) {
            //Log.d("geo test", "currentLoadType is null");
            if (adapter == null) {
                //Log.d("geo test", "postListAdapter is null");
                currentLoadType = LoadType.init;
                //setSubmissionSort(SubmissionSort.HOT);
                if(submissionSort==null) submissionSort = SubmissionSort.HOT;
                setActionBarSubtitle();
                task = new LoadPostListTask(activity, this, LoadType.init);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
            }
        }
        else switch (currentLoadType) {
            case init:
                //Log.d("geo test", "currentLoadType is init");
                contentView.setVisibility(View.GONE);
                mainProgressBar.setVisibility(View.VISIBLE);
                break;
            case refresh:
                //Log.d("geo test", "currentLoadType is refersh");
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                //Log.d("geo test", "currentLoadType is extend");
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                adapter.setLoadingMoreItems(true);
                break;
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                refreshList();
                return true;
            case R.id.action_sort:
                showSortPopup(activity.findViewById(R.id.action_sort));
                return true;
            case R.id.action_search:
                showSearchDialog();
                return true;
            case R.id.action_hide_read:
                removeClickedPosts();
                return true;
            case R.id.action_switch_view:
                showViewsPopupSafe();
                return true;
            case R.id.action_view_sidebar:
                if(subreddit == null || subreddit.equalsIgnoreCase("all") || subreddit.equalsIgnoreCase("popular")) {
                    String string = (subreddit==null) ? "the front page" : "/r/" + subreddit;
                    setSnackbar(ToastUtils.showSnackbar(getSnackbarParentView(), "No sidebar for " + string));
                }
                else {
                    SubredditSidebarDialogFragment dialog = new SubredditSidebarDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("subreddit", subreddit);
                    dialog.setArguments(bundle);
                    dialog.show(activity.getFragmentManager(), "dialog");
                }
                return true;
            case R.id.action_view_synced:
                showSyncedReddits();
                break;
            case R.id.action_pending_actions:
                showPendingActions();
                break;
            case R.id.action_clear_synced:
                final String messageEnd;
                if(subreddit==null) {
                    messageEnd = "the frontpage";
                }
                else {
                    messageEnd = "'" + subreddit + "'";
                }
                final String message = "Delete all synced posts, comments, images and articles for " + messageEnd + "?";
                new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.MyAlertDialogStyle)).setMessage(message).setNegativeButton("No", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final PleaseWaitDialogFragment dialogFragment = new PleaseWaitDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("message", "Clearing synced data for " + messageEnd);
                        dialogFragment.setArguments(args);
                        dialogFragment.show(activity.getSupportFragmentManager(), "dialog");

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                String name = (subreddit==null) ? "frontpage" : subreddit;
                                if(isMulti) {
                                    name = "multi=" + name;
                                }
                                CleaningUtils.clearAllSyncedData(activity, name);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                dialogFragment.dismiss();
                                String toastMessage = "Synced data for " + messageEnd + " cleared";
                                ToastUtils.showToast(activity, toastMessage);
                            }
                        }.execute();
                    }
                }).show();
                return true;
            case R.id.action_sync_profiles:
                showSyncProfiles();
                return true;
            case R.id.action_custom_sync:
                showCustomSyncDialog();
                return true;
            case R.id.action_sync_posts:
                addToSyncQueue();
                return true;
            case R.id.action_submit_post:
                submitPost(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submitPost(boolean fabClicked) {
        if (MyApplication.currentUser == null) {
            setSnackbar(ToastUtils.showSnackbar(getSnackbarParentView(), "Must be logged in to submit a post"));
        } else {
            if (fabClicked) {
                setFabSubmitOptionsVisible(true);
            } else {
                showSubmitPopupSafe();
            }
        }
    }

    public void showSearchDialog() {
        hideAllFabOptions();
        SearchRedditDialogFragment searchDialog = new SearchRedditDialogFragment();
        Bundle args = new Bundle();
        args.putString("subreddit", subreddit);
        args.putBoolean("isMulti", isMulti);
        searchDialog.setArguments(args);
        searchDialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void addToSyncQueue() {
        addToSyncQueue(null);
    }

    public void addToSyncQueue(SyncProfileOptions syncOptions) {
        hideAllFabOptions();
        String toastMessage;
        if (GeneralUtils.isNetworkAvailable(activity)) {
            boolean syncOverWifiOnly = (syncOptions==null) ? MyApplication.syncOverWifiOnly : syncOptions.isSyncOverWifiOnly();
            if (syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(activity)) {
                toastMessage = "Syncing over mobile data connection is disabled";
            } else {
                String filename = (subreddit == null) ? "frontpage" : subreddit;
                toastMessage = filename + " added to sync queue";
                Intent intent = new Intent(activity, DownloaderService.class);
                intent.putExtra("sort", submissionSort);
                intent.putExtra("time", timeSpan);
                intent.putExtra("subreddit", subreddit);
                intent.putExtra("isMulti", isMulti);
                intent.putExtra("isOther", isOther);
                intent.putExtra("syncOptions", syncOptions);
                activity.startService(intent);
                checkOfflineModeExplanation();
            }
        } else {
            toastMessage = "Network connection unavailable";
        }
        ToastUtils.showToast(activity, toastMessage);
    }

    private void checkOfflineModeExplanation() {
        if (!MyApplication.showedOfflineModeInfo) {
            MyApplication.showedOfflineModeInfo = true;
            SharedPreferences.Editor editor = MyApplication.prefs.edit();
            editor.putBoolean("offlineInfo", MyApplication.showedOfflineModeInfo);
            editor.apply();

            InfoDialogFragment.showDialog(activity.getSupportFragmentManager(),
                    activity.getResources().getString(R.string.about_offline_mode_title),
                    activity.getResources().getString(R.string.about_offline_mode));
        }
    }

    private void showSubmitPopupSafe() {
        try {
            showSubmitPopup(activity.findViewById(R.id.action_sort));
        } catch (Exception e) {
            try {
                showSubmitPopup(activity.findViewById(R.id.action_refresh));
            } catch (Exception x) {
                //Crashlytics.log("Null anchor used for showSubmitPopup()");
                Log.e(TAG, "Null anchor used for showSubmitPopup()");
                x.printStackTrace();
            }
        }
    }

    private void showSubmitPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_type);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_submit_link:
                        startSubmitActivity(SubmitType.link);
                        return true;
                    case R.id.action_submit_text:
                        startSubmitActivity(SubmitType.self);
                        return true;
                    //case R.id.action_submit_image:
                    //    return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void startSubmitActivity(SubmitType submitType) {
        hideAllFabOptions();
        Intent intent = new Intent(activity, SubmitActivity.class);
        intent.putExtra("subreddit", subreddit);
        intent.putExtra("submitType", submitType);
        startActivity(intent);
    }

    public void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_posts_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hot:
                        refreshList(SubmissionSort.HOT, null);
                        return true;
                    case R.id.action_sort_new:
                        refreshList(SubmissionSort.NEW, null);
                        return true;
                    case R.id.action_sort_rising:
                        refreshList(SubmissionSort.RISING, null);
                        return true;
                    case R.id.action_sort_top:
                        tempSort = SubmissionSort.TOP;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_controversial:
                        tempSort = SubmissionSort.CONTROVERSIAL;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortTimePopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_posts_sort_time);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hour:
                        refreshList(tempSort, TimeSpan.HOUR);
                        return true;
                    case R.id.action_sort_day:
                        refreshList(tempSort, TimeSpan.DAY);
                        return true;
                    case R.id.action_sort_week:
                        refreshList(tempSort, TimeSpan.WEEK);
                        return true;
                    case R.id.action_sort_month:
                        refreshList(tempSort, TimeSpan.MONTH);
                        return true;
                    case R.id.action_sort_year:
                        refreshList(tempSort, TimeSpan.YEAR);
                        return true;
                    case R.id.action_sort_all:
                        refreshList(tempSort, TimeSpan.ALL);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        setActionBarSubtitle();
        registerReceivers();
    }

    @Override
    public void onDetach() {
        unregisterReceivers();
        super.onDetach();
    }

    private void registerReceivers() {
        submittedReceiver = new RedditItemSubmittedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RedditItemSubmittedReceiver.POST_SUBMISSION);
        activity.registerReceiver(submittedReceiver, filter);
    }

    private void unregisterReceivers() {
        activity.unregisterReceiver(submittedReceiver);
    }

    @Override
    public void extendList() {
        currentLoadType = LoadType.extend;
        adapter.setLoadingMoreItems(true);
        task = new LoadPostListTask(activity, this, LoadType.extend);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean hasFabNavigation() {
        return true;
    }

    @Override
    public void refreshList() {
        refreshList(submissionSort, timeSpan);
    }

    public void refreshList(SubmissionSort sort, TimeSpan time) {
        dismissSnackbar();
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        updateCurrentViewType();
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadPostListTask(activity, this, LoadType.refresh, sort, time);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        hideAllFabOptions();
    }

    public void changeSubreddit(String subreddit, boolean isMulti, boolean isOther) {
        this.isMulti = isMulti;
        this.isOther = isOther;
        if(isMulti) {
            subreddit = subreddit.substring(subreddit.indexOf('=') + 1);
        }
        changeSubreddit(subreddit);
    }

    public void changeSubreddit(String subreddit) {
        dismissSnackbar();
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.init;
        this.subreddit = subreddit;
        this.submissionSort = SubmissionSort.HOT;
        this.timeSpan = null;
        updateCurrentViewType();
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        task = new LoadPostListTask(activity, this, LoadType.init);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        hideAllFabOptions();
        activity.invalidateOptionsMenu();
        activity.expandToolbar();
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
        setActionBarTitle();
    }

    public void setSubmissionSort(SubmissionSort sort) {
        this.timeSpan = null;
        this.submissionSort = sort;
        setActionBarSubtitle();
    }

    public void setSubmissionSort(TimeSpan time) {
        this.submissionSort = tempSort;
        this.timeSpan = time;
        setActionBarSubtitle();
    }

    public void setActionBarTitle() {
        String title = (subreddit == null) ? "frontpage" : subreddit;
        if(isMulti) title  = title.concat(" multi");
        activity.getSupportActionBar().setTitle(title);
    }

    public void setActionBarSubtitle() {
        String subtitle;
        if(MyApplication.offlineModeEnabled) {
            subtitle = "loading";
        }
        else {
            if(submissionSort == null) submissionSort = SubmissionSort.HOT;
            subtitle = submissionSort.value();
            if (timeSpan!=null) subtitle += ": " + timeSpan.value();
        }
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void setActionBarSubtitle(String subtitle) {
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

}

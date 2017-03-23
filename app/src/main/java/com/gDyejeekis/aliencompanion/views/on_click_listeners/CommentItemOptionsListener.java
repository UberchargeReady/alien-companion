package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.asynctask.SaveOfflineActionTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.models.offline_actions.DownvoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.NoVoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnsaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UpvoteAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by George on 8/15/2015.
 */
public class CommentItemOptionsListener implements View.OnClickListener {

    //private Activity activity;
    private Context context;
    private Comment comment;
    private RecyclerView.Adapter recyclerAdapter;
    private BaseAdapter adapter;

    public CommentItemOptionsListener(Context context, Comment comment, BaseAdapter adapter) {
        this.context = context;
        this.comment = comment;
        this.adapter = adapter;
    }

    public CommentItemOptionsListener(Context context, Comment comment, RecyclerView.Adapter adapter) {
        this.context = context;
        this.comment = comment;
        this.recyclerAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upvote:
                UserActionType actionType;
                LoadUserActionTask task;
                SaveOfflineActionTask task1;
                if(MyApplication.currentUser!=null) {

                    if (comment.getLikes().equals("true")) {
                        comment.setLikes("null");
                        comment.setScore(comment.getScore() - 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (comment.getLikes().equals("false"))
                            comment.setScore(comment.getScore() + 2);
                        else comment.setScore(comment.getScore() + 1);
                        comment.setLikes("true");
                        actionType = UserActionType.upvote;
                    }

                    if (adapter != null) adapter.notifyDataSetChanged();
                    else recyclerAdapter.notifyDataSetChanged();

                    if(GeneralUtils.isNetworkAvailable(context)) {

                        task = new LoadUserActionTask(context, comment.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if(actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, comment.getFullName(), comment.getBody());
                        }
                        else {
                            action = new UpvoteAction(accountName, comment.getFullName(), comment.getBody());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                }
                else ToastUtils.showToast(context, "Must be logged in to vote");
                break;
            case R.id.btn_downvote:
                if(MyApplication.currentUser!=null) {

                    if (comment.getLikes().equals("false")) {
                        comment.setLikes("null");
                        comment.setScore(comment.getScore() + 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (comment.getLikes().equals("true"))
                            comment.setScore(comment.getScore() - 2);
                        else comment.setScore(comment.getScore() - 1);
                        comment.setLikes("false");
                        actionType = UserActionType.downvote;
                    }

                    if (adapter != null) adapter.notifyDataSetChanged();
                    else recyclerAdapter.notifyDataSetChanged();

                    if(GeneralUtils.isNetworkAvailable(context)) {

                        task = new LoadUserActionTask(context, comment.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if(actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, comment.getFullName(), comment.getBody());
                        }
                        else {
                            action = new DownvoteAction(accountName, comment.getFullName(), comment.getBody());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                }
                else ToastUtils.showToast(context, "Must be logged in to vote");
                break;
            case R.id.btn_reply:
                if(MyApplication.currentUser!=null) {
                    Intent intent = new Intent(context, SubmitActivity.class);
                    intent.putExtra("submitType", SubmitType.comment);
                    intent.putExtra("originalComment", comment);
                    context.startActivity(intent);
                }
                else ToastUtils.showToast(context, "Must be logged in to reply");
                break;
            case R.id.btn_view_user:
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("username", comment.getAuthor());
                context.startActivity(intent);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v);
                break;
        }
    }

    private void showMoreOptionsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        if(MyApplication.currentUser!=null && comment.getAuthor().equals(MyApplication.currentUser.getUsername())) popupMenu.inflate(R.menu.menu_comment_more_options_account);
        else popupMenu.inflate(R.menu.menu_comment_more_options);

        if(comment.isSaved()) {
            Menu menu = popupMenu.getMenu();
            menu.findItem(R.id.action_save).setTitle("Unsave");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        if(recyclerAdapter instanceof RedditItemListAdapter) ((RedditItemListAdapter) recyclerAdapter).remove(comment);
                        LoadUserActionTask task = new LoadUserActionTask(context, comment.getFullName(), UserActionType.delete);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        return true;
                    case R.id.action_edit:
                        Intent intent = new Intent(context, SubmitActivity.class);
                        intent.putExtra("submitType", SubmitType.comment);
                        intent.putExtra("originalComment", comment);
                        intent.putExtra("edit", true);
                        context.startActivity(intent);
                        return true;
                    case R.id.action_copy_link:
                        String commentLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                                + "?comment=" + comment.getIdentifier();
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Comment permalink", commentLink);
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_copy_text:
                        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clip = ClipData.newPlainText("Comment body", comment.getBody()); //TODO: escape markdown/HTML foramtting (maybe)
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_share:
                        commentLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                                + "?comment=" + comment.getIdentifier();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, commentLink);
                        sendIntent.setType("text/plain");
                        context.startActivity(Intent.createChooser(sendIntent, "Share comment to.."));
                        return true;
                    case R.id.action_open_browser:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                        + "?comment=" + comment.getIdentifier()));
                        context.startActivity(intent);
                        return true;
                    case R.id.action_save:
                        if(MyApplication.currentUser!=null) {
                            UserActionType actionType;
                            if(comment.isSaved()) {
                                comment.setSaved(false);
                                actionType = UserActionType.unsave;
                            }
                            else {
                                comment.setSaved(true);
                                actionType = UserActionType.save;
                            }
                            if(GeneralUtils.isNetworkAvailable(context)) {
                                task = new LoadUserActionTask(context, comment, actionType);
                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                            else {
                                OfflineUserAction action;
                                String accountname = MyApplication.currentAccount.getUsername();
                                if(actionType == UserActionType.save) {
                                    action = new SaveAction(accountname, comment.getFullName(), comment.getBody());
                                }
                                else {
                                    action = new UnsaveAction(accountname, comment.getFullName(), comment.getBody());
                                }
                                SaveOfflineActionTask task1 = new SaveOfflineActionTask(context, action);
                                task1.execute();
                            }
                        }
                        else ToastUtils.showToast(context, "Must be logged in to save");
                        return true;
                    case R.id.action_report:
                        if(MyApplication.currentUser!=null) {
                            ReportDialogFragment dialog = new ReportDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("postId", comment.getFullName());
                            dialog.setArguments(bundle);
                            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        }
                        else ToastUtils.showToast(context, "Must be logged in to report");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }
}

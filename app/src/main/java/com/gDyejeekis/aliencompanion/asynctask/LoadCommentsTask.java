package com.gDyejeekis.aliencompanion.asynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.ToolbarActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.utils.ThumbnailLoader;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    public static final String TAG = "LoadCommentsTask";

    private Exception exception;
    private Context context;
    private PostFragment fragment;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private boolean initialLoad;
    private String offlineSubtitle;
    private int linkedCommentIndex = -1;

    public LoadCommentsTask(Context context, PostFragment fragment, boolean initialLoad) {
        this.context = context;
        this.fragment = fragment;
        this.initialLoad =  initialLoad;
    }

    private Submission readPostFromFile(final String postId) {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().equals(postId);
            }
        };

        try {
            File postFile = null;
            List<File> files = new ArrayList<>();
            StorageUtils.listFilesRecursive(GeneralUtils.getSyncedRedditDataDir(context),
                    fileFilter, files);
            for(File file : files) {
                // always keep the last updated matching post file
                if(postFile == null || file.lastModified() > postFile.lastModified())
                    postFile = file;
            }

            Log.d(TAG, "reading comments from " + postFile.getAbsolutePath());
            FileInputStream fis = new FileInputStream(postFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Submission post = (Submission) ois.readObject();
            offlineSubtitle = "synced " + ConvertUtils.getSubmissionAge((double) postFile.lastModified() / 1000);
            return post;
        } catch (Exception e) {
            offlineSubtitle = "not synced";
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected List<Comment> doInBackground(Void... unused) {
        try {
            List<Comment> comments;
            if(MyApplication.offlineModeEnabled) {
                Submission post = readPostFromFile(fragment.post.getIdentifier());
                if(post==null) {
                    throw new IOException("File not found ("+ fragment.post.getIdentifier()+")");
                }
                else {
                    fragment.showFullCommentsButton = false;
                    String thumbUri = fragment.post.getThumbnail();
                    fragment.post = post;
                    fragment.post.setThumbnail(thumbUri);
                    comments = post.getSyncedComments();
                }
            }
            else {
                if(fragment.post==null && fragment.redditVideoUrl!=null) {
                    String url = GeneralUtils.getFinalUrlRedirect(fragment.redditVideoUrl);
                    fragment.post = LinkUtils.getRedditPostFromUrl(url);
                }

                Comments cmnts = new Comments(httpClient, MyApplication.currentUser);
                int depth = (fragment.post.getLinkedCommentId()!=null) ? 999 : MyApplication.initialCommentDepth;
                comments = cmnts.ofSubmission(fragment.post, fragment.post.getLinkedCommentId(), fragment.post.getParentsShown(), depth,
                        MyApplication.initialCommentCount, fragment.tempSort);
            }

            if(this.fragment.post.getLinkedCommentId()!=null) {
                int i = 0;
                for(Comment comment : comments) {
                    i++;
                    if(this.fragment.post.getLinkedCommentId().equals(comment.getIdentifier())) {
                        linkedCommentIndex = i;
                        break;
                    }
                }
            }
            Comments.indentCommentTree(comments);

            return comments;
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        if(MyApplication.accountChanges) {
            MyApplication.accountChanges = false;
            GeneralUtils.saveAccountChanges(context);
        }

        try {
            PostFragment fragment = (PostFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("postFragment");
            this.fragment = fragment;
            this.fragment.progressBar.setVisibility(View.GONE);
            this.fragment.swipeRefreshLayout.setRefreshing(false);
            this.fragment.commentsLoaded = true;
            if (offlineSubtitle!=null) {
                this.fragment.setActionBarSubtitle(offlineSubtitle);
            }
            if (!MyApplication.noThumbnails && this.fragment.post.getThumbnailObject() == null) {
                ThumbnailLoader.preloadThumbnail(this.fragment.post, context);
            } //TODO: monitor this for any exceptions

            if (exception != null) {
                this.fragment.postAdapter.notifyItemChanged(0);
                if (exception instanceof IOException) {
                    ToastUtils.showSnackbar(this.fragment.getSnackbarParentView(), "No synced comments found");
                } else {
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LoadCommentsTask.this.fragment.refreshPostAndComments();
                        }
                    };
                    this.fragment.setSnackbar(ToastUtils.showSnackbar(this.fragment.getSnackbarParentView(), "Error loading comments", "Retry", listener, Snackbar.LENGTH_INDEFINITE));
                }
            }
            else {
                ((Activity) context).invalidateOptionsMenu();
                this.fragment.setActionBarTitle();
                this.fragment.postAdapter.commentsRefreshed(this.fragment.post, comments);
                if (initialLoad) {
                    if (linkedCommentIndex != -1) {
                        this.fragment.mLayoutManager.scrollToPosition(linkedCommentIndex);
                    }
                } else {
                    ((ToolbarActivity) context).expandToolbar();
                    this.fragment.mLayoutManager.scrollToPosition(0);
                    this.fragment.setCommentSort(this.fragment.tempSort);
                    this.fragment.setActionBarSubtitle(offlineSubtitle);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}

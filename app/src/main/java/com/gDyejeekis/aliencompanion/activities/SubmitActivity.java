package com.gDyejeekis.aliencompanion.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.broadcast_receivers.RedditItemSubmittedReceiver;
import com.gDyejeekis.aliencompanion.fragments.ComposeMessageFragment;
import com.gDyejeekis.aliencompanion.fragments.submit_fragments.SubmitCommentFragment;
import com.gDyejeekis.aliencompanion.fragments.submit_fragments.SubmitImageFragment;
import com.gDyejeekis.aliencompanion.fragments.submit_fragments.SubmitLinkFragment;
import com.gDyejeekis.aliencompanion.fragments.submit_fragments.SubmitTextFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.models.RedditItem;

public class SubmitActivity extends ToolbarActivity implements DialogInterface.OnClickListener {

    private Fragment fragment;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);
        }
        initToolbar();

        setupFragment();
    }

    private void setupFragment() {
        if(getFragmentManager().findFragmentByTag("submitFragment") == null) {
            SubmitType submitType = (SubmitType) getIntent().getSerializableExtra("submitType");
            //Fragment fragment = null;
            switch (submitType) {
                case link:
                    getSupportActionBar().setTitle("Submit link");
                    fragment = new SubmitLinkFragment();
                    break;
                case self:
                    getSupportActionBar().setTitle("Submit text");
                    fragment = new SubmitTextFragment();
                    break;
                case image:
                    getSupportActionBar().setTitle("Submit image");
                    fragment = new SubmitImageFragment();
                    break;
                case comment:
                    getSupportActionBar().setTitle("Submit reply");
                    fragment = new SubmitCommentFragment();
                    break;
                case message:
                    getSupportActionBar().setTitle("Compose message");
                    fragment = new ComposeMessageFragment();
                    break;
            }
            getFragmentManager().beginTransaction().add(R.id.fragmentHolder, fragment, "submitFragment").commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_submit_post, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        String message = "";
        boolean showDialog = false;
        if(fragment instanceof SubmitCommentFragment) {
            if(getIntent().getBooleanExtra("edit", false)) {
                message = "Discard changes?";
            }
            else {
                message = "Discard comment?";
            }
            showDialog = ((SubmitCommentFragment) fragment).displayConfirmDialog();
        }
        else if(fragment instanceof SubmitLinkFragment) {
            message = "Discard post?";
            showDialog = ((SubmitLinkFragment) fragment).displayConfirmDialog();
        }
        else if(fragment instanceof SubmitTextFragment) {
            message = "Discard post?";
            showDialog = ((SubmitTextFragment) fragment).displayConfirmDialog();
        }
        else if(fragment instanceof ComposeMessageFragment) {
            message = "Discard message?";
            showDialog = ((ComposeMessageFragment) fragment).displayConfirmDialog();
        }

        if(showDialog) {
            new AlertDialog.Builder(this).setMessage(message).setPositiveButton("Yes", this).setNegativeButton("No", null).show();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        super.onBackPressed();
    }

    public void sendBroadcast(RedditItem item) {
        if (item instanceof Submission)
            sendBroadcast((Submission) item);
        else if (item instanceof Comment)
            sendBroadcast((Comment) item);
    }

    private void sendBroadcast(Comment comment) {
        boolean edit = getIntent().getBooleanExtra("edit", false);
        String action = edit ? RedditItemSubmittedReceiver.COMMENT_EDIT : RedditItemSubmittedReceiver.COMMENT_SUBMISSION;
        Intent broadcastIntent = new Intent(action);
        broadcastIntent.putExtra("comment", comment);
        broadcastIntent.putExtra("position", getIntent().getIntExtra("position", -1));
        sendBroadcast(broadcastIntent);
    }

    private void sendBroadcast(Submission post) {
        boolean edit = getIntent().getBooleanExtra("edit", false);
        String action = edit ? RedditItemSubmittedReceiver.POST_SELF_TEXT_EDIT : RedditItemSubmittedReceiver.POST_SUBMISSION;
        Intent broadcastIntent = new Intent(action);
        broadcastIntent.putExtra("post", post);
        sendBroadcast(broadcastIntent);
    }

}

package com.dyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.dyejeekis.aliencompanion.Models.Thumbnail;
import com.dyejeekis.aliencompanion.Utils.MyHtmlTagHandler;
import com.dyejeekis.aliencompanion.Utils.MyLinkMovementMethod;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.enums.PostViewType;
import com.squareup.picasso.Picasso;

/**
 * Created by sound on 8/28/2015.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView domain2;
    public TextView fullUrl;
    public TextView selfText;
    public TextView selfTextCard;
    public TextView postDets1;
    public TextView postDets2;
    public TextView scoreText;
    public TextView commentsText;
    public ImageView postImage;
    public ImageView commentsIcon;
    public LinearLayout layoutSelfTextPreview;
    public LinearLayout commentsButton;
    public LinearLayout linkButton;
    public LinearLayout fullComments;
    public LinearLayout layoutPostOptions;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView viewUser;
    public ImageView openBrowser;
    public ImageView moreOptions;
    public ProgressBar commentsProgress;

    private int upvoteResource, downvoteResource, saveResource, hideResource, moreResource, commentsResource;

    public PostViewType viewType;

    //private static final int clickedColor = Color.GRAY;
    private static final int clickedColor = MainActivity.textHintColor;
    private static int upvoteColor, downvoteColor;

    public PostViewHolder(View itemView, PostViewType type) {
        super(itemView);
        this.viewType = type;
        upvoteColor = Color.parseColor("#ff8b60");
        downvoteColor = Color.parseColor("#9494ff");

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        commentsText = (TextView) itemView.findViewById(R.id.textView_comments);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
        upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_postOptions);
        downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

        if(viewType == PostViewType.listItem || viewType == PostViewType.details || MainActivity.nightThemeEnabled) {
            viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
            openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
            upvoteResource = R.mipmap.ic_action_upvote_white;
            downvoteResource = R.mipmap.ic_action_downvote_white;
            saveResource = R.mipmap.ic_action_save_white;
            hideResource = R.mipmap.ic_action_hide_white;
            moreResource = R.mipmap.ic_action_more_vertical_white;
            commentsResource = R.mipmap.ic_chat_bubble_outline_light_grey_24dp;
        }
        else {
            upvoteResource = R.mipmap.ic_action_upvote_grey;
            downvoteResource = R.mipmap.ic_action_downvote_grey;
            saveResource = R.mipmap.ic_action_save_grey;
            hideResource = R.mipmap.ic_action_hide_grey;
            moreResource = R.mipmap.ic_action_more_vertical_grey;
            commentsResource = R.mipmap.ic_chat_bubble_outline_grey_24dp;
        }
        switch (viewType) {
            case listItem:
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                commentsIcon = (ImageView) itemView.findViewById(R.id.imgView_commentsIcon);
                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                postDets2 = (TextView) itemView.findViewById(R.id.textView_dets2);
                break;
            case details:
                selfText = (TextView) itemView.findViewById(R.id.txtView_selfText);
                fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
                commentsProgress = (ProgressBar) itemView.findViewById(R.id.pBar_comments);
                break;
            case smallCards:
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                scoreText = (TextView) itemView.findViewById(R.id.textView_score);
                break;
            case cards:
                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
                fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
                layoutSelfTextPreview = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
                selfTextCard = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
                scoreText = (TextView) itemView.findViewById(R.id.textView_score);
                break;
            case cardDetails:
                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
                commentsProgress = (ProgressBar) itemView.findViewById(R.id.pBar_comments);
                domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
                fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
                layoutSelfTextPreview = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
                selfTextCard = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
                scoreText = (TextView) itemView.findViewById(R.id.textView_score);
                break;
        }
    }

    public void bindModel(Context context, Submission post) {

        title.setText(post.getTitle());

        Thumbnail postThumbnail = post.getThumbnailObject();
        if(postThumbnail == null) postThumbnail = new Thumbnail();
        //ImageLoader.preloadThumbnail(post, context);
        //Thumbnail postThumbnail = post.getThumbnailObject();
        //TODO: clean this
        if(viewType == PostViewType.smallCards) {
            postImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            if(post.isSelf()) linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            else {
                linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                if (post.isNSFW() && !MainActivity.prefs.getBoolean("showNSFWthumb", false)) {
                    postImage.setImageResource(R.drawable.nsfw2);
                }
                else if(postThumbnail.hasThumbnail()){
                    try {
                        //Get Post Thumbnail
                        Picasso.with(context).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
                else postImage.setImageResource(R.drawable.noimage);
            }
        }
        else {
            if (postThumbnail.hasThumbnail()) {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                if (postThumbnail.isSelf()) {
                    postImage.setImageResource(R.drawable.self_default2);
                    //postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else if (post.isNSFW() && !MainActivity.prefs.getBoolean("showNSFWthumb", false)) {
                    //postImage.setImageResource(R.drawable.nsfw2);
                    postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else {
                    try {
                        //Get Post Thumbnail
                        Picasso.with(context).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
            }
            else postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
        }

        if(viewType == PostViewType.listItem || viewType == PostViewType.cards || viewType == PostViewType.smallCards) {
            if(post.isClicked()) {
                title.setTextColor(clickedColor);
                if(viewType == PostViewType.listItem) commentsText.setTextColor(clickedColor);
            }
            else {
                title.setTextColor(MainActivity.textColor);
                if(viewType == PostViewType.listItem) commentsText.setTextColor(MainActivity.textColor);
            }
        }

        switch (viewType) {
            case listItem:
                commentsIcon.setImageResource(commentsResource);
                layoutPostOptions.setBackgroundColor(MainActivity.currentColor);
                bindPostList(context, post);
                break;
            case smallCards:
                moreOptions.setImageResource(moreResource);
                bindPostCards(context, post);
                break;
            case cards:
                moreOptions.setImageResource(moreResource);
                bindPostCards(context, post);
                if(post.isSelf()) {
                    linkButton.setVisibility(View.GONE);
                    try {
                        String text = ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML())).toString();
                        if(text.length()>200) text = text.substring(0, 200) + " ...";
                        selfTextCard.setText(text);
                        layoutSelfTextPreview.setVisibility(View.VISIBLE);
                    } catch (NullPointerException e) {
                        layoutSelfTextPreview.setVisibility(View.GONE);
                    }
                }
                else {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    linkButton.setVisibility(View.VISIBLE);
                    domain2.setText(post.getDomain());
                    domain2.setTextColor(MainActivity.linkColor);
                    fullUrl.setText(post.getURL());
                }
                break;
            case cardDetails:
                moreOptions.setImageResource(moreResource);
                bindPostCards(context, post);
                if(post.isSelf()) {
                    linkButton.setVisibility(View.GONE);
                    if(post.getSelftextHTML()!=null) {
                        layoutSelfTextPreview.setVisibility(View.VISIBLE);
                        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML(), null, new MyHtmlTagHandler()));
                        stringBuilder = ConvertUtils.modifyURLSpan(context, stringBuilder);
                        selfTextCard.setText(stringBuilder);
                        selfTextCard.setMovementMethod(MyLinkMovementMethod.getInstance());
                    }
                    else layoutSelfTextPreview.setVisibility(View.GONE);
                }
                else {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    linkButton.setVisibility(View.VISIBLE);
                    domain2.setText(post.getDomain());
                    domain2.setTextColor(MainActivity.linkColor);
                    fullUrl.setText(post.getURL());
                }
                break;
        }
    }

    private void bindPostList(Context context, Submission post) {
        String dets1 = post.getScore() + " · " + post.agePrepared + " · " + post.getAuthor();
        setIconsAndScoreText(context, postDets1, dets1, post);
        String dets2 = (post.isSelf()) ? post.getDomain() : post.getSubreddit() + " · " + post.getDomain();
        postDets2.setText(dets2);
        commentsText.setText(String.valueOf(post.getCommentCount()));

        if(post.isNSFW()) appendNsfwLabel(context, postDets2);
    }

    private void bindPostCards(Context context, Submission post) {
        String dets = post.getAuthor() + " · " + post.agePrepared + " · ";
        if(post.isSelf()) dets += post.getDomain();
        else dets += post.getSubreddit() + " · " + post.getDomain();
        postDets1.setText(dets);
        //scoreText.setText(post.getScore() + " score");
        setIconsAndScoreText(context, scoreText, post.getScore() + " score", post);
        commentsText.setText(post.getCommentCount() + " comments");

        if(post.isNSFW()) appendNsfwLabel(context, postDets1);
    }

    private void appendNsfwLabel(Context context, TextView textView) {
        SpannableString nsfwSpan = new SpannableString(" · NSFW");
        nsfwSpan.setSpan(new TextAppearanceSpan(context, R.style.nsfwLabel), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(nsfwSpan);
    }

    private void setIconsAndScoreText(Context context, TextView textView, String text, Submission post) {
        if(MainActivity.currentUser != null) {
            //check user vote
            if (post.getLikes().equals("true")) {
                int index = text.indexOf(" ");
                SpannableString spannable = new SpannableString(text);
                spannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                textView.setText(spannable);
                upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                downvote.setImageResource(downvoteResource);
            } else if (post.getLikes().equals("false")) {
                int index = text.indexOf(" ");
                SpannableString spannable = new SpannableString(text);
                spannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                textView.setText(spannable);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
            } else {
                textView.setText(text);
                textView.setTextColor(MainActivity.textHintColor);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(downvoteResource);
            }
            //check saved post
            if(post.isSaved()) save.setImageResource(R.mipmap.ic_action_save_yellow);
            else save.setImageResource(saveResource);
            //check hidden post
            if(post.isHidden()) hide.setImageResource(R.mipmap.ic_action_hide_red);
            else hide.setImageResource(hideResource);
        }
        else {
            textView.setText(text);
            upvote.setImageResource(upvoteResource);
            downvote.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
        }
    }

    public void setCardButtonsListener(PostItemOptionsListener listener) {
        upvote.setOnClickListener(listener);
        downvote.setOnClickListener(listener);
        save.setOnClickListener(listener);
        hide.setOnClickListener(listener);
        moreOptions.setOnClickListener(listener);
    }

    public void showPostOptions(PostItemOptionsListener listener) {
        layoutPostOptions.setVisibility(View.VISIBLE);
        upvote.setOnClickListener(listener);
        downvote.setOnClickListener(listener);
        save.setOnClickListener(listener);
        hide.setOnClickListener(listener);
        viewUser.setOnClickListener(listener);
        openBrowser.setOnClickListener(listener);
        moreOptions.setOnClickListener(listener);
    }

    public void hidePostOptions() {
        layoutPostOptions.setVisibility(View.GONE);
    }
}
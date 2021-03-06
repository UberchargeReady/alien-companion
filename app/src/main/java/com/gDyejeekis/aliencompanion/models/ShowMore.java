package com.gDyejeekis.aliencompanion.models;

import android.text.SpannableStringBuilder;

import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;

/**
 * Created by sound on 8/28/2015.
 */
public class ShowMore implements RedditItem {

    public static final String ID = "SHOW_MORE_ITEM";

    public ShowMore() {}

    @Override
    public String getIdentifier() {
        return ID;
    }

    public int getViewType() {
        return RedditItemListAdapter.VIEW_TYPE_SHOW_MORE;
    }

    public String getThumbnail() {
        return null;
    }

    public void setThumbnailObject(Thumbnail thumbnailObject) {

    }

    public Thumbnail getThumbnailObject() {
        return null;
    }

    public String getMainText() {
        return "SHOW MORE";
    }

    public SpannableStringBuilder getPreparedText() {
        return null;
    }

    public void storePreparedText(SpannableStringBuilder stringBuilder) {}
}

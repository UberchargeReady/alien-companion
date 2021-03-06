package com.gDyejeekis.aliencompanion.models.nav_drawer;

import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

/**
 * Created by George on 6/26/2015.
 */
public class NavDrawerSubredditItem implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_SUBREDDIT_ITEM;
    }

    private String name;

    public NavDrawerSubredditItem(String subreddit) {
        this.name = subreddit;
    }

    public NavDrawerSubredditItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String subreddit) {
        this.name = subreddit;
    }
}

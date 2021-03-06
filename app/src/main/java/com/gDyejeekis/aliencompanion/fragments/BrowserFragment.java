package com.gDyejeekis.aliencompanion.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.BrowserActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class BrowserFragment extends Fragment {

    public static final String TAG = "BrowserFragment";

    public WebView webView;
    private ProgressBar progressBar;
    private AppCompatActivity activity;
    private Submission post;
    private String url;
    private String domain;
    private boolean addRedditAccount;
    private Bundle webViewBundle;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //if (Uri.parse(url).getHost().equals("www.example.com")) {
            //    // This is my web site, so do not override; let my WebView load the page
            //    return false;
            //}
            //// Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //startActivity(intent);
            //return true;
            if (addRedditAccount) {
                if (url.startsWith(RedditOAuth.REDIRECT_URI)) {
                    Log.d(TAG, "OAuth redirect url: " + url);
                    boolean success = RedditOAuth.parseRedirectUrl(url);
                    if (success) MainActivity.setupAccount = true;
                    else ToastUtils.showToast(activity, "Failed to retrieve authorization code");
                    activity.finish();
                    return true;
                }
            } else {
                LinkHandler linkHandler = new LinkHandler(activity, url);
                linkHandler.setBrowserActive(true);
                Log.d(TAG, "URL: " + url + "\ndomain: " + linkHandler.getDomain());
                return linkHandler.handleIt();
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            try {
                if (!addRedditAccount) {
                    setActionbarTitle(LinkUtils.getDomainName(url));
                    updateMenuItems();
                } else {
                    setActionbarTitle("Add account");
                }
            } catch (Exception e) {
                e.printStackTrace();
                setActionbarTitle(url);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            try {
                if (!addRedditAccount) {
                    updateMenuItems();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView webView, int progress) {
            if(progress < 100 && progressBar.getVisibility() == ProgressBar.GONE){
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
            progressBar.setProgress(progress);
            if(progress == 100) {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        this.activity = (AppCompatActivity) getActivity();
        if (activity instanceof BrowserActivity) {
            this.post = ((BrowserActivity) activity).post;
            this.url = ((BrowserActivity) activity).url;
            this.domain = ((BrowserActivity) activity).domain;
            this.addRedditAccount = ((BrowserActivity) activity).addRedditAccount;
        } else {
            this.url = activity.getIntent().getStringExtra("url");
            this.domain = activity.getIntent().getStringExtra("domain");
            this.addRedditAccount = activity.getIntent().getBooleanExtra("addRedditAccount", false);
            if(this.domain == null || this.domain.trim().isEmpty()) {
                try {
                    this.domain = LinkUtils.getDomainName(url);
                } catch (Exception e) {
                    this.domain = "";
                }
            }
        }
        if (addRedditAccount)
            CleaningUtils.clearCookies(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        try {
            activity.getSupportActionBar().setTitle(domain);
            if (post != null)
                activity.getSupportActionBar().setSubtitle(post.getCommentCount() + " comments");
        } catch (NullPointerException e) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar4);
        progressBar.getIndeterminateDrawable().setColorFilter(MyApplication.colorSecondary, PorterDuff.Mode.SRC_IN);
        webView = (WebView) view.findViewById(R.id.webView);

        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        //if(addRedditAccount) settings.setAppCacheEnabled(false);
        //else settings.setAppCacheEnabled(true);
        //settings.setAppCacheMaxSize(20 * 1024 * 1024);
        //settings.setAppCachePath(activity.getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        //if(!GeneralUtils.isNetworkAvailable(activity) && MainActivity.offlineModeEnabled) settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        //else settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);

        if(webViewBundle == null) {
            webView.setWebChromeClient(new MyWebChromeClient());
            webView.loadUrl(url);
        }
        else {
            webView.restoreState(webViewBundle);
        }
        return view;
    }

    @Override
    public void onPause() {
        webView.onPause();
        webViewBundle = new Bundle();
        webView.saveState(webViewBundle);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                goBack();
                return true;
            case R.id.action_forward:
                goForward();
                return true;
            case R.id.action_open_browser:
                try {
                    final String currentUrl = webView.getUrl();
                    Uri uri = currentUrl != null ? Uri.parse(currentUrl) : Uri.parse(url);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showToast(activity, "Failed to parse URL");
                }
                return true;
            case R.id.action_refresh:
                webView.reload();
                return true;
            case R.id.action_load_cache:
                if(activity instanceof BrowserActivity) {
                    boolean loadFromCache = ((BrowserActivity) activity).loadFromCache;
                    if(loadFromCache) {
                        loadLiveVersion();
                    }
                    else {
                        loadCachedCopy();
                    }
                }
                return true;
            case R.id.action_share_url:
                GeneralUtils.shareUrl(activity, "Share via..", webView.getUrl());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCachedCopy() {
        // load article from synced data
        if (MyApplication.offlineModeEnabled && ((BrowserActivity) activity).syncedArticleExists()) {
            ((BrowserActivity) activity).loadSyncedArticle();
        }
        // load cached copy from cache
        else {
            ((BrowserActivity) activity).loadFromCache = true;
            ((BrowserActivity) activity).loadSyncedArticle = false;
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            webView.reload();
        }
        activity.invalidateOptionsMenu();
    }

    private void loadLiveVersion() {
        ((BrowserActivity) activity).loadFromCache = false;
        ((BrowserActivity) activity).loadSyncedArticle = false;
        activity.invalidateOptionsMenu();
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.reload();
    }

    public void setActionbarTitle(String title) {
        try {
            activity.getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
        webView.goBack();
    }

    public void goForward() {
        webView.goForward();
    }

    private void updateMenuItems() {
        //Log.d(TAG, "can go back " + webView.canGoBack());
        //Log.d(TAG, "can go forward " + webView.canGoForward());
        BrowserActivity browserActivity = (BrowserActivity) activity;
        browserActivity.canGoBack = webView.canGoBack();
        browserActivity.canGoForward = webView.canGoForward();
        browserActivity.invalidateOptionsMenu();
    }

}

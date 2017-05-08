package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.fragments.BrowserFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class BrowserActivity extends SwipeBackActivity {

    public boolean loadFromCache = false;

    public boolean canGoBack, canGoForward;

    @Override
    public void onBackPressed() {
        BrowserFragment fragment = (BrowserFragment) getFragmentManager().findFragmentById(R.id.fragment_browser);
        if(fragment.webView.canGoBack()) {
            fragment.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        initToolbar();

        SwipeBackLayout swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe);
        swipeBackLayout.setEdgeTrackingEnabled(MyApplication.swipeSetting);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        if(loadFromCache) {
            menu.findItem(R.id.action_load_cache).setTitle("Load live version");
        }

        MenuItem goBack = menu.findItem(R.id.action_back);
        goBack.setEnabled(canGoBack);
        goBack.setIcon(canGoBack ? R.drawable.ic_arrow_back_white_24dp : R.drawable.ic_arrow_back_white_disabled_24dp);
        MenuItem goForward = menu.findItem(R.id.action_forward);
        goForward.setEnabled(canGoForward);
        goForward.setIcon(canGoForward ? R.drawable.ic_arrow_forward_white_24dp : R.drawable.ic_arrow_forward_white_disabled_24dp);

        if(getIntent().getSerializableExtra("post") == null) {
            menu.findItem(R.id.action_comments).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            //onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }
}

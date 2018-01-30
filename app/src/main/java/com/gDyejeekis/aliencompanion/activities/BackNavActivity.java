package com.gDyejeekis.aliencompanion.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by George on 6/12/2015.
 */
public abstract class BackNavActivity extends AppCompatActivity {

    // TODO: 1/30/2018 maybe embed this into toolbar activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

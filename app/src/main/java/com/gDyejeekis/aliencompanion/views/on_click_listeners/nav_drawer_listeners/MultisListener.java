package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.asynctask.RefreshUserMultisTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by sound on 1/23/2016.
 */
public class MultisListener extends NavDrawerListener {

    public MultisListener(MainActivity activity) {
        super(activity, null);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layoutToggle) {
            getAdapter().toggleMultiredditItems();
        } else if (v.getId() == R.id.layoutEdit) {
            refreshMultis();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    private void refreshMultis() {
        if (MyApplication.currentUser == null) {
            ToastUtils.showToast(getActivity(), "Must be logged in to do that");
        } else {
            ToastUtils.showToast(getActivity(), "Refreshing multis...");
            RefreshUserMultisTask task = new RefreshUserMultisTask(getActivity());
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}

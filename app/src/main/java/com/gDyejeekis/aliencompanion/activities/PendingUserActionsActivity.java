package com.gDyejeekis.aliencompanion.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.views.adapters.PendingActionsAdapter;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.services.PendingActionsService;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;

/**
 * Created by sound on 4/9/2016.
 */
public class PendingUserActionsActivity extends ToolbarActivity {

    private RecyclerView pendingActionsView;
    private PendingActionsAdapter adapter;
    private BroadcastReceiver receiver;
    public static boolean isActive;
    public static int editedIndex = -1;
    public static OfflineUserAction newAction;

    @Override
    public void onCreate(Bundle bundle) {
        isActive = true;
        MyApplication.applyCurrentTheme(this);
        super.onCreate(bundle);
        setContentView(R.layout.activity_pending_actions);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);
        }
        initToolbar();

        pendingActionsView = (RecyclerView) findViewById(R.id.recyclerView_pendingActions);
        adapter = new PendingActionsAdapter(this);
        pendingActionsView.setLayoutManager(new LinearLayoutManager(this));
        pendingActionsView.setAdapter(adapter);
        pendingActionsView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String actionId = intent.getStringExtra("actionId");
                boolean success = intent.getBooleanExtra("success", false);
                adapter.notifyActionResult(actionId, success);
            }
        };
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(PendingActionsService.RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    public PendingActionsAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(editedIndex != -1) {
            adapter.actionEdited(editedIndex, newAction);
            editedIndex = -1;
            newAction = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pending_actions, menu);
        return true;
    }

    public static boolean checkServiceRunning(Context context) {
        if(PendingActionsService.isRunning) {
            ToastUtils.showToast(context, "Pending actions execution in progress");
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_execute_all_offline_actions:
                if(checkServiceRunning(this)) {
                    return true;
                }
                adapter.executeAllActions();
                return true;
            case R.id.action_cancel_all_offline_actions:
                if(checkServiceRunning(this)) {
                    return true;
                }
                adapter.cancelAllActions();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        isActive = false;
        super.onDestroy();
    }
}

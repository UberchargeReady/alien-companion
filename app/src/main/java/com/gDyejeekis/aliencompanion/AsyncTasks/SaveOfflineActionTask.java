package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 4/18/2016.
 */
public class SaveOfflineActionTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private Exception exception;
    private OfflineUserAction offlineAction;

    public SaveOfflineActionTask(Context context, OfflineUserAction offlineAction) {
        this.context = context;
        this.offlineAction = offlineAction;
    }

    @Override
    public Boolean doInBackground(Void... unused) {
        try {
            List<OfflineUserAction> offlineActions;
            File file = new File(context.getFilesDir(), MyApplication.OFFLINE_USER_ACTIONS_FILENAME);
            try {
                offlineActions = (List<OfflineUserAction>) GeneralUtils.readObjectFromFile(file);
            } catch (Exception e) {
                offlineActions = new ArrayList<>();
            }

            for(OfflineUserAction action : offlineActions) {
                if(action.equals(offlineAction)) {
                    return true;
                }
                else if(action.getActionType() == offlineAction.getActionType()) {
                    String id1 = action.getActionId().substring(action.getActionId().lastIndexOf('-') + 1);
                    String id2 = offlineAction.getActionId().substring(action.getActionId().lastIndexOf('-') + 1);
                    if(id1.equals(id2)) {
                        offlineActions.set(offlineActions.indexOf(action), offlineAction);
                        GeneralUtils.writeObjectToFile(offlineActions, file);
                        return false;
                    }
                }
            }

            offlineActions.add(offlineAction);
            GeneralUtils.writeObjectToFile(offlineActions, file);
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPostExecute(Boolean actionAlreadyExists) {
        if(exception != null) {
            ToastUtils.displayShortToast(context, "Error queueing offline action");
        }
        else {
            if (actionAlreadyExists) {
                ToastUtils.displayShortToast(context, "Action already queued");
            }
            else {
                ToastUtils.displayShortToast(context, offlineAction.getActionName() + " queued");
                if(!MyApplication.pendingOfflineActions) {
                    MyApplication.pendingOfflineActions = true;
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    editor.putBoolean("pendingActions", MyApplication.pendingOfflineActions);
                    editor.commit();
                }
            }
        }
    }
}

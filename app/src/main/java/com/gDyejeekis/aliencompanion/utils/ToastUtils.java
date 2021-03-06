package com.gDyejeekis.aliencompanion.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by George on 6/19/2015.
 */
public class ToastUtils {

    public static final String TAG = "ToastUtils";

    public static void showSnackbarOverToast(Context context, String message) {
        showSnackbarOverToast(context, message, null, null);
    }

    public static void showSnackbarOverToast(Context context, String message, String actionText, View.OnClickListener listener) {
        if(context instanceof Activity) {
            showSnackbar(((Activity) context).getCurrentFocus(), message, actionText, listener);
        }
        else {
            showToast(context, message);
        }
    }

    // Toasts

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message, int length) {
        try {
            Toast.makeText(context,  message, length).show();
        } catch (Exception e) {}
    }

    // Snackbars

    public static Snackbar showSnackbar(View view, String text) {
        return showSnackbar(view, text, Snackbar.LENGTH_SHORT);
    }

    public static Snackbar showSnackbar(View view, String text, int duration) {
        try {
            Snackbar snackbar = Snackbar.make(view, text, duration);
            TextView txtv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            txtv.setTextColor(Color.WHITE);
            txtv.setMaxLines(3);
            snackbar.show();
            return snackbar;
        } catch (Exception e) {}
        return null;
    }

    public static Snackbar showSnackbar(View view, String text, String actionText, View.OnClickListener listener) {
        return showSnackbar(view, text, actionText, listener, Snackbar.LENGTH_LONG);
    }

    public static Snackbar showSnackbar(View view, String text, String actionText, View.OnClickListener listener, int duration) {
        try {
            Snackbar snackbar = Snackbar.make(view, text, duration);
            if (listener != null) {
                snackbar.setAction(actionText, listener);
                snackbar.setActionTextColor(MyApplication.colorSecondary);
            }
            TextView txtv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            txtv.setTextColor(Color.WHITE);
            txtv.setMaxLines(3);
            snackbar.show();
            return snackbar;
        } catch (Exception e) {}
        return null;
    }

}

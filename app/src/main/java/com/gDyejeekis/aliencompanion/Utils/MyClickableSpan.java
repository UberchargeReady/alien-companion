package com.gDyejeekis.aliencompanion.utils;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by George on 8/16/2015.
 */
public abstract class MyClickableSpan extends ClickableSpan {

    public abstract boolean onLongClick(View widget);
}

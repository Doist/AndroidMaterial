package io.doist.material.widget.utils;

import android.content.Context;
import android.view.ContextThemeWrapper;

/**
 * A {@link ContextThemeWrapper} with public {@link #getThemeResId()}.
 */
public class MaterialContextThemeWrapper extends ContextThemeWrapper {
    private int mThemeResId;

    public MaterialContextThemeWrapper(Context base, int themeResId) {
        super(base, themeResId);
        mThemeResId = themeResId;
    }

    @Override
    public void setTheme(int themeResId) {
        super.setTheme(themeResId);
        mThemeResId = themeResId;
    }

    public int getThemeResId() {
        return mThemeResId;
    }
}

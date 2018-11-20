package io.doist.material.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import androidx.annotation.DrawableRes;
import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialCheckedTextView extends CheckedTextView {
    private static final boolean sNative = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.CHECKED_TEXT_VIEW,
            MaterialWidgetHandler.Styleable.TEXT_VIEW,
            MaterialWidgetHandler.Styleable.VIEW
    };

    public MaterialCheckedTextView(Context context) {
        this(context, null);
    }

    @SuppressLint("InlinedApi")
    public MaterialCheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs,
             Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN ? android.R.attr.checkedTextViewStyle : 0);
    }

    public MaterialCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }

    @Override
    public void setCheckMarkDrawable(@DrawableRes int resId) {
        if (sNative) {
            super.setCheckMarkDrawable(resId);
        } else {
            super.setCheckMarkDrawable(MaterialWidgetHandler.getDrawable(this, resId));
        }
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(@DrawableRes int left, @DrawableRes int top,
                                                        @DrawableRes int right, @DrawableRes int bottom) {
        if (sNative) {
            super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        } else {
            super.setCompoundDrawablesWithIntrinsicBounds(
                    MaterialWidgetHandler.getDrawable(this, left), MaterialWidgetHandler.getDrawable(this, top),
                    MaterialWidgetHandler.getDrawable(this, right), MaterialWidgetHandler.getDrawable(this, bottom));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(@DrawableRes int start, @DrawableRes int top,
                                                                @DrawableRes int end, @DrawableRes int bottom) {
        if (sNative) {
            // Don't set compound drawables relative because it breaks spinner popup width calculation.
            boolean isRtl = getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            int left = isRtl ? end : start;
            int right = isRtl ? start : end;
            super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        } else {
            super.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    MaterialWidgetHandler.getDrawable(this, start), MaterialWidgetHandler.getDrawable(this, top),
                    MaterialWidgetHandler.getDrawable(this, end), MaterialWidgetHandler.getDrawable(this, bottom));
        }
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        if (sNative) {
            super.setBackgroundResource(resId);
        } else {
            super.setBackground(MaterialWidgetHandler.getDrawable(this, resId));
        }
    }
}

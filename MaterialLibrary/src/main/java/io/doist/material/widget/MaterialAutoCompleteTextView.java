package io.doist.material.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import androidx.annotation.DrawableRes;
import io.doist.material.widget.utils.MaterialWidgetHandler;

@SuppressLint("AppCompatCustomView")
public class MaterialAutoCompleteTextView extends AutoCompleteTextView {
    private static final boolean sNative = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.TEXT_VIEW,
            MaterialWidgetHandler.Styleable.VIEW
    };

    public MaterialAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public MaterialAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public MaterialAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
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
            super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
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

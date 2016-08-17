package io.doist.material.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialSpinner extends AppCompatSpinner {
    private static final boolean sNative = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.VIEW,
            MaterialWidgetHandler.Styleable.SPINNER
    };

    public MaterialSpinner(Context context) {
        this(context, null);
    }

    public MaterialSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.spinnerStyle);
    }

    public MaterialSpinner(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, -1);
    }

    public MaterialSpinner(Context context, int mode) {
        this(context, null, android.R.attr.spinnerStyle, mode);
    }

    public MaterialSpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle, mode);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        if (sNative) {
            super.setBackgroundResource(resId);
        } else {
            super.setBackground(MaterialWidgetHandler.getDrawable(this, resId));
        }
    }

    @Override
    public void setPopupBackgroundResource(@DrawableRes int resId) {
        if (sNative) {
            super.setPopupBackgroundResource(resId);
        } else {
            super.setPopupBackgroundDrawable(MaterialWidgetHandler.getDrawable(this, resId));
        }
    }
}

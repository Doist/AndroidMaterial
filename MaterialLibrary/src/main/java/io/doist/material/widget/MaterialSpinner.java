package io.doist.material.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialSpinner extends AppCompatSpinner {
    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.VIEW,
            MaterialWidgetHandler.Styleable.POPUP_WINDOW,
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
}

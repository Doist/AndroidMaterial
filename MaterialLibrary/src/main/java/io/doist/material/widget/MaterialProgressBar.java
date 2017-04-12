package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialProgressBar extends ProgressBar {
    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.PROGRESS_BAR,
            MaterialWidgetHandler.Styleable.VIEW
    };

    public MaterialProgressBar(Context context) {
        this(context, null);
    }

    public MaterialProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyle);
    }

    public MaterialProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

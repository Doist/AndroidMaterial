package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialLinearLayout extends LinearLayout {
    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.VIEW
    };

    public MaterialLinearLayout(Context context) {
        this(context, null);
    }

    public MaterialLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

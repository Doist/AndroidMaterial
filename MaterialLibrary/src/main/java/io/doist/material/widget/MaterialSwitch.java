package io.doist.material.widget;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import io.doist.material.R;
import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialSwitch extends SwitchCompat {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialSwitch(Context context) {
        super(context);
    }

    public MaterialSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchStyle);
    }

    public MaterialSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

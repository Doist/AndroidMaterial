package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialToggleButton extends ToggleButton {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialToggleButton(Context context) {
        this(context, null);
    }

    public MaterialToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyleToggle);
    }

    public MaterialToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialButton extends RobotoButton {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialButton(Context context) {
        super(context);
    }

    public MaterialButton(Context context, AttributeSet attrs) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables));
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, android.R.attr.buttonStyle, sHiddenStyleables);
    }

    public MaterialButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

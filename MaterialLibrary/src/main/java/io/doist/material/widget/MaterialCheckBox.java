package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialCheckBox extends CheckBox {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_COMPOUND_BUTTON,
                                                       MaterialWidgetHandler.STYLEABLE_TEXT_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialCheckBox(Context context) {
        super(context);
    }

    public MaterialCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public MaterialCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

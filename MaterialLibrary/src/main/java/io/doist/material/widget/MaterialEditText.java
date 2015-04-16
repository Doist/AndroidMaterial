package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialEditText extends RobotoEditText {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_TEXT_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialEditText(Context context) {
        this(context, null);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

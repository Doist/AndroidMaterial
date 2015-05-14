package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialAutoCompleteTextView extends RobotoAutoCompleteTextView {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_TEXT_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_VIEW};

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
}

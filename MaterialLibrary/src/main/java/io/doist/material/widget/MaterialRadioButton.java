package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialRadioButton extends RadioButton {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_COMPOUND_BUTTON,
                                                       MaterialWidgetHandler.STYLEABLE_TEXT_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialRadioButton(Context context) {
        this(context, null);
    }

    public MaterialRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.radioButtonStyle);
    }

    public MaterialRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

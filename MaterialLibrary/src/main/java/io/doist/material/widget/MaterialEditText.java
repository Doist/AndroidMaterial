package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialEditText extends EditText {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_TEXT_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialEditText(Context context) {
        super(context);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

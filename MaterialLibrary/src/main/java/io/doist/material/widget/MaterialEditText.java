package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialEditText extends EditText {
    private static final int[] sHiddenAttrs = {android.R.attr.background};

    public MaterialEditText(Context context) {
        super(context);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenAttrs), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenAttrs);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenAttrs);
    }
}

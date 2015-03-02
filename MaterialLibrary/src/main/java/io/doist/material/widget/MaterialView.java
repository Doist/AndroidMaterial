package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialView extends View {
    private static final int[] sHiddenAttrs = {android.R.attr.background};

    public MaterialView(Context context) {
        super(context);
    }

    public MaterialView(Context context, AttributeSet attrs) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenAttrs));
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenAttrs);
        MaterialWidgetHandler.init(this, attrs, android.R.attr.buttonStyle, sHiddenAttrs);
    }

    public MaterialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenAttrs), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenAttrs);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenAttrs);
    }
}

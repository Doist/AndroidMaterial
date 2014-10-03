package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialImageView extends ImageView {
    private static final int[] sHiddenAttrs = {android.R.attr.background, android.R.attr.src};

    public MaterialImageView(Context context) {
        super(context);
    }

    public MaterialImageView(Context context, AttributeSet attrs) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenAttrs));
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenAttrs);
        MaterialWidgetHandler.init(this, attrs, 0, sHiddenAttrs);
    }

    public MaterialImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenAttrs), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenAttrs);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenAttrs);
    }
}

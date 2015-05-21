package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialFrameLayout extends FrameLayout {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_FRAME_LAYOUT};

    public MaterialFrameLayout(Context context) {
        this(context, null);
    }

    public MaterialFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

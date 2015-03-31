package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialImageView extends ImageView {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_VIEW,
                                                       MaterialWidgetHandler.STYLEABLE_IMAGE_VIEW};

    public MaterialImageView(Context context) {
        super(context);
    }

    public MaterialImageView(Context context, AttributeSet attrs) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables));
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, 0, sHiddenStyleables);
    }

    public MaterialImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialImageView extends ImageView {
    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.VIEW,
            MaterialWidgetHandler.Styleable.IMAGE_VIEW
    };

    public MaterialImageView(Context context) {
        this(context, null);
    }

    public MaterialImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialImageView(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

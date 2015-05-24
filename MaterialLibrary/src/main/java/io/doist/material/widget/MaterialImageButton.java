package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialImageButton extends ImageButton {
    private static final MaterialWidgetHandler.Styleable[] sHiddenStyleables = {
            MaterialWidgetHandler.Styleable.VIEW,
            MaterialWidgetHandler.Styleable.IMAGE_VIEW
    };

    public MaterialImageButton(Context context) {
        this(context, null);
    }

    public MaterialImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.imageButtonStyle);
    }

    public MaterialImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

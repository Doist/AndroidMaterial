package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialButton extends Button {
    public MaterialButton(Context context) {
        super(context);
    }

    public MaterialButton(Context context, AttributeSet attrs) {
        super(context, MaterialWidgetHandler.discardStyleableAttributes(attrs));
        MaterialWidgetHandler.init(this, attrs, android.R.attr.buttonStyle);
    }

    public MaterialButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, MaterialWidgetHandler.discardStyleableAttributes(attrs), defStyle);
        MaterialWidgetHandler.init(this, attrs, defStyle);
    }
}

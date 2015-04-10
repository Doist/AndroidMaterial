package io.doist.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import io.doist.material.R;
import io.doist.material.widget.utils.MaterialWidgetHandler;

public class MaterialSwitchBar extends SwitchBar {
    private static final String[] sHiddenStyleables = {MaterialWidgetHandler.STYLEABLE_VIEW};

    public MaterialSwitchBar(Context context) {
        super(context);
    }

    public MaterialSwitchBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchBarStyle);
    }

    public MaterialSwitchBar(Context context, AttributeSet attrs, int defStyle) {
        super(MaterialWidgetHandler.themifyContext(context, attrs),
              MaterialWidgetHandler.hideStyleableAttributes(attrs, sHiddenStyleables), defStyle);
        MaterialWidgetHandler.restoreStyleableAttributes(sHiddenStyleables);
        MaterialWidgetHandler.init(this, attrs, defStyle, sHiddenStyleables);
    }
}

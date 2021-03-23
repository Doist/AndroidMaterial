package io.doist.material.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

import androidx.annotation.DrawableRes;
import io.doist.material.widget.utils.MaterialWidgetHandler;

@SuppressLint("AppCompatCustomView")
public class MaterialImageButton extends ImageButton {
    private static final boolean sNative = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;

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

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        if (sNative) {
            super.setBackgroundResource(resId);
        } else {
            super.setBackground(MaterialWidgetHandler.getDrawable(this, resId));
        }
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        if (sNative) {
            super.setImageResource(resId);
        } else {
            super.setImageDrawable(MaterialWidgetHandler.getDrawable(this, resId));
        }
    }
}

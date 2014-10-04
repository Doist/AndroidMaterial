package io.doist.material.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import io.doist.material.widget.utils.Roboto;

public class RobotoTextView extends TextView {
    private boolean mAllowSetTypeface;

    public RobotoTextView(Context context) {
        super(context);
        applyRoboto(context, null, 0);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyRoboto(context, attrs, 0);
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyRoboto(context, attrs, defStyle);
    }

    private void applyRoboto(Context context, AttributeSet attrs, int defStyle) {
        mAllowSetTypeface = true;
        Roboto.apply(this, context, attrs, defStyle);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (mAllowSetTypeface) {
            super.setTypeface(tf, style);
        }
    }
}

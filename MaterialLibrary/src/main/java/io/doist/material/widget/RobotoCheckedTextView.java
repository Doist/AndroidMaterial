package io.doist.material.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import io.doist.material.widget.utils.Roboto;

public class RobotoCheckedTextView extends CheckedTextView {
    private boolean mAllowSetTypeface;

    public RobotoCheckedTextView(Context context) {
        super(context);
        applyRoboto(context, null, 0);
    }

    public RobotoCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyRoboto(context, attrs, 0);
    }

    public RobotoCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
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

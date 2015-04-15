package io.doist.material.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import io.doist.material.utils.Roboto;

public class RobotoCheckedTextView extends CheckedTextView {
    private boolean mAllowSetTypeface;

    public RobotoCheckedTextView(Context context) {
        this(context, null);
    }

    public RobotoCheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, getDefStyle());
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

    private static int getDefStyle() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return android.R.attr.checkedTextViewStyle;
        } else {
            return 0;
        }
    }
}

package io.doist.material.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import io.doist.material.utils.Roboto;

public class RobotoEditText extends EditText {
    private boolean mAllowSetTypeface;

    public RobotoEditText(Context context) {
        this(context, null);
    }

    public RobotoEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public RobotoEditText(Context context, AttributeSet attrs, int defStyle) {
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

package io.doist.material.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import io.doist.material.elevation.ElevationDelegate;

public class ElevatedTextView extends TextView {
    private ElevationDelegate mElevationDelegate;

    public ElevatedTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public ElevatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ElevatedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        mElevationDelegate = new ElevationDelegate(this, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mElevationDelegate.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mElevationDelegate.onDetachedFromWindow();
    }
}

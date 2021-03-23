package io.doist.material.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import io.doist.material.elevation.CompatElevationDelegate;

@SuppressLint("AppCompatCustomView")
public class DemoElevatedTextView extends TextView {
    private CompatElevationDelegate mCompatElevationDelegate;

    public DemoElevatedTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public DemoElevatedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DemoElevatedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        mCompatElevationDelegate = new CompatElevationDelegate(this, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mCompatElevationDelegate.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mCompatElevationDelegate.onDetachedFromWindow();
    }
}

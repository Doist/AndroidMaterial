package io.doist.material.sample;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import io.doist.material.elevation.CompatElevationDelegate;

public class ElevatedTextView extends TextView {
    private CompatElevationDelegate mCompatElevationDelegate;

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mCompatElevationDelegate = new CompatElevationDelegate(this, attrs, defStyleAttr);
        } /* else native elevation can be used. */
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mCompatElevationDelegate != null) {
            mCompatElevationDelegate.onAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mCompatElevationDelegate != null) {
            mCompatElevationDelegate.onDetachedFromWindow();
        }
    }
}

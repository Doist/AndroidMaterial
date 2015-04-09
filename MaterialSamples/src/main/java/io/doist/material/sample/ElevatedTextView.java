package io.doist.material.sample;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import io.doist.material.elevation.ElevationDelegate;

public class ElevatedTextView extends TextView implements ElevationDelegate.Host {
    private ElevationDelegate<ElevatedTextView> mElevationDelegate;

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
        mElevationDelegate = new ElevationDelegate<>(this, attrs, defStyleAttr);
    }

    @Override
    public void setBackground(Drawable background) {
        if (mElevationDelegate != null) {
            mElevationDelegate.setBackground(background);
        } else {
            super.setBackground(background);
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (mElevationDelegate != null) {
            mElevationDelegate.setPadding(left, top, right, bottom);
        } else {
            super.setPadding(left, top, right, bottom);
        }
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        if (mElevationDelegate != null) {
            mElevationDelegate.setPaddingRelative(start, top, end, bottom);
        } else {
            super.setPaddingRelative(start, top, end, bottom);
        }
    }

    @Override
    public void superSetBackground(Drawable background) {
        super.setBackground(background);
    }

    @Override
    public void superSetPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (mElevationDelegate != null) {
            mElevationDelegate.setLayoutParams(params);
        } else {
            super.setLayoutParams(params);
        }
    }

    @Override
    public void superSetLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }

    @Override
    public int getPaddingLeft() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getUnshadowedPaddingLeft();
        } else {
            return super.getPaddingLeft();
        }
    }

    @Override
    public int getPaddingTop() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getUnshadowedPaddingTop();
        } else {
            return super.getPaddingTop();
        }
    }

    @Override
    public int getPaddingRight() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getUnshadowedPaddingRight();
        } else {
            return super.getPaddingRight();
        }
    }

    @Override
    public int getPaddingBottom() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getUnshadowedPaddingBottom();
        } else {
            return super.getPaddingBottom();
        }
    }

    @Override
    public int getPaddingStart() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getUnshadowedPaddingStart();
        } else {
            return super.getPaddingStart();
        }
    }

    @Override
    public int getPaddingEnd() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getUnshadowedPaddingEnd();
        } else {
            return super.getPaddingEnd();
        }
    }
}

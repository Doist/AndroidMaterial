package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import java.io.IOException;

public class WrapperDrawable extends Drawable implements Drawable.Callback {
    private WrapperState mWrapperState;
    private boolean mMutated = false;

    public WrapperDrawable(Drawable drawable) {
        mWrapperState = createConstantState(null);
        setWrappedDrawable(drawable);
    }

    public void setWrappedDrawable(Drawable drawable) {
        mWrapperState.setDrawable(drawable, this);
    }

    public Drawable getWrappedDrawable() {
        return mWrapperState.mDrawable;
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        mWrapperState.mDrawable.inflate(r, parser, attrs);
    }

    @Override
    public void draw(Canvas canvas) {
        mWrapperState.mDrawable.draw(canvas);
    }

    @Override
    public void setChangingConfigurations(int configs) {
        mWrapperState.mChangingConfigurations = configs;
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mWrapperState.mChangingConfigurations;
    }

    @Override
    public void setDither(boolean dither) {
        mWrapperState.mDrawable.setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mWrapperState.mDrawable.setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        mWrapperState.mDrawable.setAlpha(alpha);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int getAlpha() {
        return mWrapperState.mDrawable.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mWrapperState.mDrawable.setColorFilter(cf);
    }

    @Override
    public void setColorFilter(int color, PorterDuff.Mode mode) {
        mWrapperState.mDrawable.setColorFilter(color, mode);
    }

    @Override
    public void clearColorFilter() {
        mWrapperState.mDrawable.clearColorFilter();
    }

    @Override
    public boolean isStateful() {
        return mWrapperState.mDrawable.isStateful();
    }

    @Override
    public boolean setState(int[] stateSet) {
        return mWrapperState.mDrawable.setState(stateSet);
    }

    public boolean superSetState(int[] stateSet) {
        return super.setState(stateSet);
    }

    @Override
    public int[] getState() {
        return mWrapperState.mDrawable.getState();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return false;
    }

    @Override
    public void jumpToCurrentState() {
        mWrapperState.mDrawable.jumpToCurrentState();
    }

    @Override
    public Drawable getCurrent() {
        return mWrapperState.mDrawable.getCurrent();
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mWrapperState.mDrawable.setLevel(level);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mWrapperState.mDrawable.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return mWrapperState.mDrawable.setVisible(visible, restart) |
                super.setVisible(mWrapperState.mDrawable.isVisible(), restart);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void setAutoMirrored(boolean mirrored) {
        mWrapperState.mDrawable.setAutoMirrored(mirrored);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean isAutoMirrored() {
        return mWrapperState.mDrawable.isAutoMirrored();
    }

    @Override
    public int getOpacity() {
        return mWrapperState.mDrawable.getOpacity();
    }

    @Override
    public Region getTransparentRegion() {
        return mWrapperState.mDrawable.getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return mWrapperState.mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mWrapperState.mDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return mWrapperState.mDrawable.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mWrapperState.mDrawable.getMinimumHeight();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mWrapperState.mDrawable.getPadding(padding);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTint(int tint) {
        mWrapperState.mDrawable.setTint(tint);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintList(ColorStateList tint) {
        mWrapperState.mDrawable.setTintList(tint);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mWrapperState.mDrawable.setTintMode(tintMode);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setHotspot(float x, float y) {
        mWrapperState.mDrawable.setHotspot(x, y);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        mWrapperState.mDrawable.setHotspotBounds(left, top, right, bottom);
    }

    /*
     * Wrapper drawable state.
     */

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mWrapperState = createConstantState(mWrapperState);
            mWrapperState.mDrawable.mutate();
            mMutated = true;
        }
        return this;
    }

    @Override
    public ConstantState getConstantState() {
        if (mWrapperState.canConstantState()) {
            mWrapperState.mChangingConfigurations = getChangingConfigurations();
            return mWrapperState;
        }
        return null;
    }

    protected WrapperState createConstantState(WrapperState state) {
        return new WrapperState(state);
    }

    protected static class WrapperState extends ConstantState {
        Drawable mDrawable;
        int mChangingConfigurations;
        int mChildChangingConfigurations;

        public WrapperState(WrapperState state) {
            if (state != null) {
                mDrawable = state.mDrawable;
                mChangingConfigurations = state.mChangingConfigurations;
                mChildChangingConfigurations = state.mChildChangingConfigurations;;
            }
        }

        public void setDrawable(Drawable drawable, WrapperDrawable owner) {
            if (mDrawable != null) {
                mDrawable.setCallback(null);
            }
            mDrawable = drawable;
            mDrawable.setCallback(owner);
            mChildChangingConfigurations = mDrawable.getChangingConfigurations();
        }

        @Override
        public Drawable newDrawable() {
            return newDrawable(null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new WrapperDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }

        protected boolean canConstantState() {
            return mDrawable.getConstantState() != null;
        }
    }

    protected WrapperDrawable(WrapperState state, Resources res) {
        mWrapperState = createConstantState(state);

        final Drawable drawable;
        if (res != null) {
            drawable = mWrapperState.mDrawable.getConstantState().newDrawable(res);
        } else {
            drawable = mWrapperState.mDrawable.getConstantState().newDrawable();
        }
        mWrapperState.setDrawable(drawable, this);
    }

    /*
     * Overrides from Drawable.Callback.
     */

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }
}

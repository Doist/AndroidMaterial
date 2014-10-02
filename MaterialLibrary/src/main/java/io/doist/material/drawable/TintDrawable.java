package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;

public class TintDrawable extends WrapperDrawable {
    private WeakReference<Context> mContext;

    private TintState mTintState;
    private boolean mMutated = false;

    public TintDrawable(Context context, Drawable drawable) {
        super(drawable);
        mContext = new WeakReference<>(context);
        mTintState = (TintState) getConstantState();
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);

        final Context context = mContext != null ? mContext.get() : null;

        final TypedArray a;
        if (context != null) {
            a = context.obtainStyledAttributes(attrs, R.styleable.TintDrawable);
        } else {
            a = r.obtainAttributes(attrs, R.styleable.TintDrawable);
        }

        try {
            final TypedValue v = new TypedValue();
            if (a.getValue(R.styleable.TintDrawable_android_tint, v)) {

                if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT
                        && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                    setTintColorInner(a.getColor(R.styleable.TintDrawable_android_tint, Color.TRANSPARENT));
                } else {
                    mTintState.mTintColorStateList = a.getColorStateList(R.styleable.TintDrawable_android_tint);
                    setTintColorInner(mTintState.mTintColorStateList.getDefaultColor());
                }
            }
        }
        finally {
            a.recycle();
        }
    }

    @Override
    public boolean isStateful() {
        // If tint has multiple states, the drawable is stateful.
        return super.isStateful() || (mTintState.mTintColorStateList != null && mTintState.mTintColorEnabled);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (mTintState.mTintColorStateList != null) {
            final int defaultColor = mTintState.mTintColorStateList.getDefaultColor();
            final int tintColor = mTintState.mTintColorStateList.getColorForState(state, defaultColor);

            return setTintColorInner(tintColor);
        }

        return false;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf != null) {
            super.setColorFilter(cf);
            mTintState.mTintColorEnabled = false;
        } else {
            setTintColorInner(mTintState.mTintColor);
        }
    }

    @Override
    public void clearColorFilter() {
        setColorFilter(null);
    }

    public void setTintColor(int color) {
        setTintColorInner(color);
    }

    private boolean setTintColorInner(int color) {
        if (mTintState.mTintColor != color || !mTintState.mTintColorEnabled) {
            mTintState.mTintColor = color;
            mTintState.mTintColorEnabled = true;

            final ColorFilter cf = color != Color.TRANSPARENT ?
                                   new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN) : null;
            super.setColorFilter(cf);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            super.mutate();

            mMutated = true;
        }
        return this;
    }

    @Override
    protected WrapperState createConstantState(WrapperState state) {
        return new TintState(state, this);
    }

    protected static class TintState extends WrapperState {
        int mTintColor = Color.TRANSPARENT;
        ColorStateList mTintColorStateList;
        boolean mTintColorEnabled = false;

        public TintState(WrapperState state, WrapperDrawable owner) {
            super(state, owner);

            if (state != null) {
                mTintColor = ((TintState) state).mTintColor;
                mTintColorStateList = ((TintState) state).mTintColorStateList;
                mTintColorEnabled = ((TintState) state).mTintColorEnabled;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new TintDrawable(this);
        }
    }

    protected TintDrawable(TintState state) {
        super(state);
        mTintState = (TintState) getConstantState();
    }
}

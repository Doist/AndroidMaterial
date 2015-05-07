package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
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
        this(context, drawable, null);
    }
    public TintDrawable(Context context, Drawable drawable, int tint) {
        this(context, drawable, ColorStateList.valueOf(tint));
    }
    public TintDrawable(Context context, Drawable drawable, ColorStateList tint) {
        super(drawable);
        mContext = new WeakReference<>(context);
        mTintState = (TintState) getConstantState();
        mTintState.mTint = tint;
        mTintState.mTintUpdate = mTintState.mTint != null;
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
                // Tint was successfully retrieved.

                if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT
                        && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                    mTintState.mTint = ColorStateList.valueOf(
                            a.getColor(R.styleable.TintDrawable_android_tint, Color.TRANSPARENT));
                } else {
                    mTintState.mTint = a.getColorStateList(R.styleable.TintDrawable_android_tint);
                }
                mTintState.mTintUpdate = true;
            }
        }
        finally {
            a.recycle();
        }
    }

    @Override
    public boolean isStateful() {
        // If tint has multiple states, the drawable is stateful.
        return super.isStateful() || (mTintState.mTint != null);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (mTintState.mTint != null) {
            mTintState.mTintUpdate = true;
            invalidateSelf();
            return true;
        }

        return false;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // Only apply a color filter, if it is not null,
        // or if there is no tint applied.
        if (cf != null || mTintState.mTint == null) {
            setColorFilterInner(cf);
        }
    }

    private void setColorFilterInner(ColorFilter cf) {
        mTintState.mTint = null;
        mTintState.mTintMode = null;
        mTintState.mTintUpdate = false;
        super.setColorFilter(cf);
    }

    @Override
    public void clearColorFilter() {
        setColorFilterInner(null);
    }

    @Override
    public void setTintList(ColorStateList tint) {
        if (mTintState.mTint != tint) {
            mTintState.mTint = tint;
            mTintState.mTintUpdate = true;
            invalidateSelf();
        }
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        if (mTintState.mTintMode != tintMode) {
            mTintState.mTintMode = tintMode;
            mTintState.mTintUpdate = true;
            invalidateSelf();
        }
    }

    private void updateTintFilter() {
        if (mTintState.mTintUpdate && mTintState.mTint != null) {
            mTintState.mTintUpdate = false;

            if (mTintState.mTintMode == null) {
                // Default tint mode.
                mTintState.mTintMode = PorterDuff.Mode.SRC_IN;
            }

            super.setColorFilter(new PorterDuffColorFilter(
                    mTintState.mTint.getColorForState(getState(), mTintState.mTint.getDefaultColor()),
                    mTintState.mTintMode));
        }
    }

    @Override
    public void draw(Canvas canvas) {
        updateTintFilter();
        super.draw(canvas);
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            TintState tintState = (TintState) getConstantState();

            if (mTintState.mTint != null) {
                // Clone mTint if not null.
                final Parcel p = Parcel.obtain();
                mTintState.mTint.writeToParcel(p, 0);
                p.setDataPosition(0);
                tintState.mTint = ColorStateList.CREATOR.createFromParcel(p);
            }

            if (mTintState.mTintMode != null) {
                tintState.mTintMode = PorterDuff.Mode.values()[mTintState.mTintMode.ordinal()];
            }

            tintState.mTintUpdate = tintState.mTint != null;

            mTintState = tintState;
            mMutated = true;
        }
        return this;
    }

    @Override
    protected WrapperState createConstantState(WrapperState state) {
        return new TintState(state);
    }

    protected static class TintState extends WrapperState {
        ColorStateList mTint;
        PorterDuff.Mode mTintMode;
        boolean mTintUpdate;

        public TintState(WrapperState state) {
            super(state);

            if (state != null) {
                mTint = ((TintState) state).mTint;
                mTintMode = ((TintState) state).mTintMode;
                mTintUpdate = ((TintState) state).mTintUpdate;
            }
        }

        @Override
        public void setDrawable(Drawable drawable, WrapperDrawable owner) {
            if (drawable.getClass() == TintDrawable.class) {
                // Chained instances of TintDrawables are not possible and thus are unnecessary.
                drawable = ((WrapperState)drawable.getConstantState()).mDrawable;
            }
            super.setDrawable(drawable, owner);
        }

        @Override
        public Drawable newDrawable() {
            return newDrawable(null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new TintDrawable(this, res);
        }
    }

    protected TintDrawable(TintState state, Resources res) {
        super(state, res);
        mTintState = (TintState) getConstantState();
    }
}

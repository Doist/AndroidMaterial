package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;

public class TintDrawable extends WrapperDrawable {
    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

    private WeakReference<Context> mContext;

    private TintState mTintState;

    private boolean mMutated = false;

    public TintDrawable(Context context, Drawable drawable) {
        this(context, drawable, null);
    }

    protected TintDrawable(TintState state, Resources res) {
        super(state, res);
        mTintState = (TintState) getConstantState();
        updateTint();
        updateAlpha();
    }

    public TintDrawable(Context context, Drawable drawable, int tint) {
        this(context, drawable, ColorStateList.valueOf(tint));
    }

    public TintDrawable(Context context, Drawable drawable, ColorStateList tint) {
        super(drawable);
        mContext = new WeakReference<>(context);
        mTintState = (TintState) getConstantState();
        mTintState.mTint = tint;
        mTintState.mTintEnabled = true;
        updateTint();
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
                mTintState.mTintEnabled = true;
            }

            // Initialize base alpha.
            mTintState.mBaseAlpha = (int) (a.getFloat(R.styleable.TintDrawable_android_alpha, 1.0f) * 255 + .5f);
            mTintState.mUseAlpha = mTintState.mBaseAlpha;
        }
        finally {
            a.recycle();
        }

        updateTint();
        updateAlpha();
    }

    @Override
    public boolean isStateful() {
        // Drawable is stateful or tint is enable and has multiple states.
        return super.isStateful() ||
                (mTintState.mTintEnabled && mTintState.mTint != null && mTintState.mTint.isStateful());
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (mTintState.mTintEnabled && mTintState.mTint != null) {
            updateTint();
            return true;
        }

        return false;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        superSetColorFilter(cf);
        mTintState.mTintEnabled = cf == null;
        updateTint();
    }

    private void superSetColorFilter(ColorFilter cf) {
        if (mTintState.mDrawable instanceof ColorDrawable) {
            Paint paint =
                    (Paint) ReflectionUtils.getDeclaredFieldValue(ColorDrawable.class, "mPaint", mTintState.mDrawable);
            paint.setColorFilter(cf);
        } else {
            super.setColorFilter(cf);
        }
    }

    @Override
    public void setTint(int tint) {
        setTintList(ColorStateList.valueOf(tint));
    }

    @Override
    public void setTintList(ColorStateList tint) {
        if (mTintState.mTint != tint) {
            mTintState.mTint = tint;
            updateTint();
        }
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        if (mTintState.mTintMode != tintMode) {
            mTintState.mTintMode = tintMode;
            updateTint();
        }
    }

    private void updateTint() {
        if (mTintState.mTintEnabled && mTintState.mTint != null) {
            superSetColorFilter(
                    new PorterDuffColorFilter(
                            mTintState.mTint.getColorForState(getState(), mTintState.mTint.getDefaultColor()),
                            mTintState.mTintMode != null ? mTintState.mTintMode : DEFAULT_TINT_MODE));
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (mTintState.mUseAlpha != alpha) {
            mTintState.mUseAlpha = alpha;
            updateAlpha();
        }
    }

    private void updateAlpha() {
        super.setAlpha(mTintState.mBaseAlpha * mTintState.mUseAlpha >> 8);
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            // Get constant state, because we are creating a new one in super.mutate().
            TintState tintState = (TintState) getConstantState();

            if (mTintState.mTint != null) {
                // Clone mTint if not null.
                final Parcel p = Parcel.obtain();
                mTintState.mTint.writeToParcel(p, 0);
                p.setDataPosition(0);
                tintState.mTint = ColorStateList.CREATOR.createFromParcel(p);
            }
            tintState.mTintMode = mTintState.mTintMode;
            tintState.mTintEnabled = mTintState.mTintEnabled;
            tintState.mBaseAlpha = mTintState.mBaseAlpha;
            tintState.mUseAlpha = mTintState.mUseAlpha;

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
        boolean mTintEnabled;
        int mBaseAlpha = 255;
        int mUseAlpha = 255;

        public TintState(WrapperState state) {
            super(state);

            if (state != null) {
                TintState tintState = (TintState) state;
                mTint = tintState.mTint;
                mTintMode = tintState.mTintMode;
                mTintEnabled = tintState.mTintEnabled;
                mBaseAlpha = tintState.mBaseAlpha;
                mUseAlpha = tintState.mUseAlpha;
            }
        }

        @Override
        public void setDrawable(Drawable drawable, WrapperDrawable owner) {
            if (drawable.getClass().equals(TintDrawable.class)) {
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

}

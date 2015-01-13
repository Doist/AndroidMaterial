package io.doist.material.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.doist.material.R;
import io.doist.material.color.ColorPalette;
import io.doist.material.drawable.RippleDrawableSimpleCompat;
import io.doist.material.drawable.TintDrawable;

public class FloatingActionButton extends ImageButton {
    private static final int SHADOW_MAX_ALPHA = 240;
    // Manually tested to better replicate the elevation result.
    private static final int DEFAULT_ELEVATION_SP = 6;
    private static final double LIGHT_ELEVATION_SP = 28;

    private boolean mInCompat;
    private ColorStateList mColor;
    private boolean mIsMini;
    private int mRadius;

    private GradientDrawable mCircleDrawable; // To change the color of the circle.
    private TintDrawable mTintDrawable; // To change the color of the circle in compat mode.

    // Used to create a shadow to fake elevation in older androids.
    private float mShadowCx;
    private float mShadowCy;
    private float mShadowRadius;
    Paint mShadowPaint;

    public FloatingActionButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("ConstantConditions")
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // Parse attributes.
        // Default attr values.
        boolean inCompat = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        Integer color = null;
        ColorStateList colorList = null;
        boolean isMini = false;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
            try {
                // Resolve if in compatibility mode.
                inCompat = ta.getBoolean(R.styleable.FloatingActionButton_inCompat, inCompat);

                // Resolve color or colorStateList.
                TypedValue v = new TypedValue();
                if (ta.getValue(R.styleable.FloatingActionButton_android_color, v)) {
                    if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT
                            && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        color = v.data;
                    } else {
                        colorList = ta.getColorStateList(R.styleable.FloatingActionButton_android_color);
                    }
                }

                // Resolve size.
                isMini = ta.getBoolean(R.styleable.FloatingActionButton_isMini, isMini);
            } finally {
                ta.recycle();
            }
        }

        mInCompat |= inCompat;

        // Set default padding.
        int padding = context.getResources().getDimensionPixelSize(R.dimen.fab_padding);
        setPadding(padding, padding, padding, padding);

        initDrawables(context);

        if (color != null) {
            setColor(color);
        } else if (colorList != null) {
            setColor(colorList);
        } else {
            // If the user didn't specify any color, try to resolve the current theme's accent color.
            setColor(ColorPalette.resolveAccentColor(context));
        }

        setIsMiniInner(isMini);

        setFocusable(true);
        setClickable(true);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        setElevation(DEFAULT_ELEVATION_SP * metrics.scaledDensity);
    }

    @SuppressLint("NewApi")
    private void initDrawables(Context context) {
        // TODO: Make this dependent on the context theme.
        int rippleColorResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                               R.color.ripple_material_light :
                               R.color.ripple_material_light_compat;
        ColorStateList rippleColor = ColorStateList.valueOf(context.getResources().getColor(rippleColorResId));

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);

        Drawable background;

        if (mInCompat) {
            // Older androids or force compat mode.
            mTintDrawable = new TintDrawable(getContext(), circleDrawable);
            RippleDrawableSimpleCompat rippleDrawable = new RippleDrawableSimpleCompat(
                    context,
                    rippleColor,
                    mTintDrawable);
            rippleDrawable.setLayerInset(0, getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
            rippleDrawable.setLayerInset(1, getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
            background = rippleDrawable;
        } else {
            // Lollipop androids.
            mCircleDrawable = circleDrawable;
            background = new InsetDrawable(
                    new RippleDrawable(
                            rippleColor,
                            circleDrawable,
                            null), getPaddingLeft(),
                    getPaddingTop(),
                    getPaddingRight(),
                    getPaddingBottom());
        }

        if (getBackground() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(background);
            } else {
                setBackgroundDrawable(background);
            }
        } else {
            throw new IllegalStateException("FloatingActionButton does not support 'android:background' attribute.");
        }
    }

    public ColorStateList getColor() {
        return mColor;
    }

    public void setColor(int color) {
        setColor(ColorStateList.valueOf(color));
    }

    @SuppressLint("NewApi")
    public void setColor(ColorStateList color) {
        mColor = color;

        if (mCircleDrawable != null) {
            mCircleDrawable.setColor(mColor);
        }

        if (mTintDrawable != null) {
            mTintDrawable.setTintList(mColor);
        }
    }

    public boolean isMini() {
        return mIsMini;
    }

    public void setIsMini(boolean isMini) {
        if (mIsMini != isMini) {
            setIsMiniInner(isMini);
            requestLayout();
            invalidate();
        }
    }

    @SuppressLint("NewApi")
    private void setIsMiniInner(boolean isMini) {
        mIsMini = isMini;
        int radiusResId = isMini ? R.dimen.fab_mini_radius : R.dimen.fab_radius;
        mRadius = getContext().getResources().getDimensionPixelOffset(radiusResId);

        if (mShadowPaint != null) {
            setElevation(getElevation());
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void setElevation(float elevation) {
        if (mInCompat) {
            if (elevation > 0) {
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                double lightElevation = LIGHT_ELEVATION_SP * metrics.scaledDensity;
                double lightAngle = Math.toDegrees(Math.atan(mRadius / lightElevation));

                mShadowRadius = (float) (Math.tan(Math.toRadians(lightAngle)) * (lightElevation + elevation));
                mShadowCx = mRadius + ((getPaddingLeft() + getPaddingRight()) / 2f);
                mShadowCy = (mRadius + ((getPaddingTop() + getPaddingBottom()) / 2f)) * 10f / 9.2f;

                if (mShadowPaint == null) {
                    mShadowPaint = new Paint();
                    mShadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
                }

                int translucentBlack = Color.argb(
                        Math.max(SHADOW_MAX_ALPHA - (int)((elevation + lightElevation) * 0.6 + .5f), 6),
                        0,
                        0,
                        0);

                mShadowPaint.setShader(new RadialGradient(
                        mShadowCx,
                        mShadowCy,
                        mShadowRadius,
                        translucentBlack,
                        Color.TRANSPARENT,
                        Shader.TileMode.MIRROR));
            } else {
                mShadowPaint = null;
            }

            requestLayout();
            invalidate();
        } else {
            super.setElevation(elevation);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                mRadius * 2 + getPaddingLeft() + getPaddingRight(),
                mRadius * 2 + getPaddingTop() + getPaddingBottom());
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT
                || params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            throw new IllegalStateException("FloatingActionButton 'android:width' and 'android:height' values must be "
                                                    + "'wrap_content'. Check 'isMini' attribute to manipulate size.");
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mShadowPaint != null) {
            canvas.drawCircle(mShadowCx, mShadowCy, mShadowRadius, mShadowPaint);
        }
        super.draw(canvas);
    }
}

package io.doist.material.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.doist.material.R;
import io.doist.material.color.ColorPalette;
import io.doist.material.drawable.RippleDrawableSimpleCompat;
import io.doist.material.drawable.TintDrawable;

public class FloatingActionButton extends ImageButton {
    private static final int DEFAULT_ELEVATION_SP = 6;

    // Used to create a shadow to fake elevation in pre-L androids.
    private ElevationManager mElevationManager;

    private ColorStateList mColor;

    private boolean mIsMini;
    private int mRadius;

    private GradientDrawable mCircleDrawable; // To change the color of the circle.
    private TintDrawable mTintDrawable; // To change the color of the circle in compat mode.

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
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

        // Parse attributes.
        // Default attr values.
        boolean inCompat = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        int elevation = (int) (DEFAULT_ELEVATION_SP * metrics.scaledDensity + .5f);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.fab_padding);
        int paddingLeft, paddingTop, paddingRight, paddingBottom;
        paddingLeft = paddingTop = paddingRight = paddingBottom = padding;
        ColorStateList color = null;
        boolean isMini = false;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
            try {
                // Resolve if in compatibility mode.
                inCompat |= ta.getBoolean(R.styleable.FloatingActionButton_inCompat, inCompat);

                elevation = ta.getDimensionPixelOffset(R.styleable.FloatingActionButton_android_elevation, elevation);

                padding = ta.getDimensionPixelSize(R.styleable.FloatingActionButton_android_padding, padding);
                paddingLeft = ta.getDimensionPixelSize(R.styleable.FloatingActionButton_android_paddingLeft, padding);
                paddingTop = ta.getDimensionPixelSize(R.styleable.FloatingActionButton_android_paddingTop, padding);
                paddingRight = ta.getDimensionPixelSize(R.styleable.FloatingActionButton_android_paddingRight, padding);
                paddingBottom = ta.getDimensionPixelSize(R.styleable.FloatingActionButton_android_paddingBottom, padding);

                // Resolve color or colorStateList.
                TypedValue v = new TypedValue();
                if (ta.getValue(R.styleable.FloatingActionButton_android_color, v)) {
                    if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT
                            && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        color = ColorStateList.valueOf(v.data);
                    } else {
                        color = ta.getColorStateList(R.styleable.FloatingActionButton_android_color);
                    }
                }

                // Resolve size.
                isMini = ta.getBoolean(R.styleable.FloatingActionButton_isMini, isMini);
            } finally {
                ta.recycle();
            }
        }

        initElevation(inCompat, elevation);

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        initDrawables(context, inCompat);

        initColor(context, color);

        internalSetIsMini(isMini);

        setFocusable(true);
        setClickable(true);
    }

    private void initElevation(boolean inCompat, float elevation) {
        if (inCompat) {
            mElevationManager = new ElevationManager(this);
        }
        setElevation(elevation);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void initDrawables(Context context, boolean inCompat) {
        // TODO: Make this theme dependent.
        int rippleColorResId = inCompat ?
                               R.color.ripple_material_light_compat :
                               R.color.ripple_material_light;
        ColorStateList rippleColor = ColorStateList.valueOf(context.getResources().getColor(rippleColorResId));

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);

        Drawable background;
        if (inCompat) {
            // Pre-L androids or force compat mode.
            mTintDrawable = new TintDrawable(getContext(), circleDrawable);
            RippleDrawableSimpleCompat rippleDrawable = new RippleDrawableSimpleCompat(
                    context,
                    rippleColor,
                    mTintDrawable);
            rippleDrawable.setLayerInset(0, paddingLeft, paddingTop, paddingRight, paddingBottom);
            rippleDrawable.setLayerInset(1, paddingLeft, paddingTop, paddingRight, paddingBottom);
            background = rippleDrawable;
        } else {
            // Lollipop androids.
            mCircleDrawable = circleDrawable;
            background = new InsetDrawable(
                    new RippleDrawable(
                            rippleColor,
                            circleDrawable,
                            null),
                    paddingLeft,
                    paddingTop,
                    paddingRight,
                    paddingBottom);
        }

        if (getBackground() != null) {
            setBackground(background);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Make sure the current padding is kept in pre-L androids.
                // Otherwise, the background drawable would override the current padding.
                setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            }
        } else {
            throw new IllegalStateException("FloatingActionButton does not support 'android:background' attribute.");
        }
    }

    private void initColor(Context context, ColorStateList color) {
        if (color != null) {
            setColor(color);
        } else {
            // If the user didn't specify any color, try to resolve the current theme's accent color.
            setColor(ColorPalette.resolveAccentColor(context));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setElevation(float elevation) {
        if (mElevationManager != null) {
            mElevationManager.setElevation(elevation);
        } else {
            super.setElevation(elevation);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public float getElevation() {
        if (mElevationManager != null) {
            return mElevationManager.getElevation();
        } else {
            return super.getElevation();
        }
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

    public ColorStateList getColor() {
        return mColor;
    }

    public void setIsMini(boolean isMini) {
        if (mIsMini != isMini) {
            internalSetIsMini(isMini);
            requestLayout();
            invalidate();
        }
    }

    @SuppressLint("NewApi")
    private void internalSetIsMini(boolean isMini) {
        mIsMini = isMini;
        int radiusResId = isMini ? R.dimen.fab_mini_radius : R.dimen.fab_radius;
        mRadius = getContext().getResources().getDimensionPixelOffset(radiusResId);

        if (mElevationManager != null) {
            mElevationManager.setRadius(mRadius);
        }
    }

    public boolean isMini() {
        return mIsMini;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);

        if (mElevationManager != null) {
            mElevationManager.setPadding(left, top);
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
    public void draw(@NonNull Canvas canvas) {
        if (mElevationManager != null) {
            mElevationManager.drawShadow(canvas);
        }
        super.draw(canvas);
    }

    private static class ElevationManager {
        // Manually tested to better replicate the elevation result.
        private static final double LIGHT_ELEVATION_SP = 30;
        private static final int SHADOW_MIN_ALPHA = 6;
        private static final int SHADOW_MAX_ALPHA = 86;
        private static final float SHADOW_DY_RATIO = 1.1f;

        private View mView;
        private DisplayMetrics mMetrics;
        private Paint mShadowPaint;

        private float mElevation;
        private int mRadius;
        private int mPaddingLeft;
        private int mPaddingTop;

        private boolean mInvalidate;
        private float mShadowCx;
        private float mShadowCy;
        private float mShadowRadius;

        public ElevationManager(View view) {
            mView = view;
            mMetrics = view.getContext().getResources().getDisplayMetrics();
            mShadowPaint = new Paint();
            mShadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        public void setElevation(float elevation) {
            if (mElevation != elevation) {
                mElevation = elevation;
                mInvalidate = true;
                // Force view to redraw the shadow.
                mView.invalidate();
                mView.requestLayout();
            }
        }

        public float getElevation() {
            return mElevation;
        }

        public void setRadius(int radius) {
            if (mRadius != radius) {
                mRadius = radius;
                mInvalidate = true;
            }
        }

        public void setPadding(int left, int top) {
            if (mPaddingLeft != left || mPaddingTop != top) {
                mPaddingLeft = left;
                mPaddingTop = top;
                mInvalidate = true;
            }
        }

        private void calculateShadow() {
            double lightElevation = LIGHT_ELEVATION_SP * mMetrics.scaledDensity;
            //double lightAngle = Math.atan(mRadius / lightElevation);
            //mShadowRadius = (float) (Math.tan(lightAngle) * (lightElevation + mElevation));
            // Calculates shadow radius in a single statement.
            mShadowRadius = (float) (mRadius * (1 + mElevation / lightElevation));

            mShadowCx = mRadius + mPaddingLeft;
            float shadowCyCentered = mRadius + mPaddingTop;
            // The shadow is on the down side of the button.
            mShadowCy = shadowCyCentered * SHADOW_DY_RATIO;
            float shadowDy = mShadowCy - shadowCyCentered;

            int translucentBlack = Color.argb(
                    Math.max(SHADOW_MAX_ALPHA - (int) ((mElevation + lightElevation) * 0.6 + .5f), SHADOW_MIN_ALPHA),
                    0,
                    0,
                    0);

            mShadowPaint.setShader(new RadialGradient(
                    mShadowCx,
                    mShadowCy,
                    mShadowRadius,
                    new int[]{translucentBlack, Color.TRANSPARENT},
                    new float[]{(mRadius - shadowDy) / mShadowRadius, 1.0f},
                    Shader.TileMode.MIRROR));
        }

        public void drawShadow(Canvas canvas) {
            if (mElevation > 0) {
                if (mInvalidate) {
                    mInvalidate = false;
                    calculateShadow();
                }
                canvas.drawCircle(mShadowCx, mShadowCy, mShadowRadius, mShadowPaint);
            }
        }
    }
}

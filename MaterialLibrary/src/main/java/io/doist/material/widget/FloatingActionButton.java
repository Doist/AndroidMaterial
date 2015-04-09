package io.doist.material.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.doist.material.R;
import io.doist.material.color.ColorPalette;
import io.doist.material.drawable.RippleMaterialDrawable;
import io.doist.material.drawable.TintDrawable;
import io.doist.material.elevation.ElevationDelegate;
import io.doist.material.widget.utils.MaterialWidgetHandler;

public class FloatingActionButton extends ImageButton implements ElevationDelegate.Host {
    private static final int DEFAULT_ELEVATION_DP = 8;

    private ColorStateList mColor;

    private boolean mIsMini;
    private int mRadius;

    private GradientDrawable mCircleDrawable; // To change the color of the circle.
    private TintDrawable mTintDrawable; // To change the color of the circle in compat mode.

    private ElevationDelegate<FloatingActionButton> mElevationDelegate;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.imageButtonStyle);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(MaterialWidgetHandler.themifyContext(context, attrs), attrs, defStyleAttr);
        init(MaterialWidgetHandler.themifyContext(context, attrs), attrs, defStyleAttr);
    }

    @SuppressWarnings("ConstantConditions")
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        boolean inCompat = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        int elevation = (int) (DEFAULT_ELEVATION_DP * context.getResources().getDisplayMetrics().density + .5f);
        ColorStateList color = null;
        boolean isMini = false;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
            try {
                if (ta.getDrawable(R.styleable.FloatingActionButton_android_background) != null) {
                    throw new IllegalStateException(
                            "FloatingActionButton does not support 'android:background' attribute.");
                }

                inCompat |= ta.getBoolean(R.styleable.FloatingActionButton_inCompat, inCompat);

                if (inCompat) {
                    elevation = ta.getDimensionPixelOffset(
                            R.styleable.FloatingActionButton_elevation,
                            elevation);
                } else {
                    elevation = ta.getDimensionPixelOffset(
                            R.styleable.FloatingActionButton_android_elevation,
                            elevation);
                }

                // Resolve color or colorStateList.
                color = ta.getColorStateList(R.styleable.FloatingActionButton_android_color);

                // Resolve size.
                isMini = ta.getBoolean(R.styleable.FloatingActionButton_isMini, isMini);
            } finally {
                ta.recycle();
            }
        }

        initElevation(inCompat, elevation);

        initDrawables(context, inCompat);

        initColor(context, color);

        internalSetIsMini(isMini);

        setFocusable(true);
        setClickable(true);
    }

    private void initElevation(boolean inCompat, float elevation) {
        if (inCompat) {
            mElevationDelegate = new ElevationDelegate<>(this);
        }
        setElevation(elevation);
    }

    @SuppressLint("NewApi")
    private void initDrawables(Context context, boolean inCompat) {
        // TODO: Make this theme dependent.
        ColorStateList rippleColor = ColorStateList.valueOf(getResources().getColor(R.color.ripple_material_light));

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);

        Drawable background;
        if (inCompat) {
            // Pre-L androids or force compat mode.
            mTintDrawable = new TintDrawable(context, circleDrawable);
            background = new RippleMaterialDrawable(context, rippleColor, mTintDrawable, null);
        } else {
            // Lollipop androids.
            mCircleDrawable = circleDrawable;
            background = new RippleDrawable(rippleColor, circleDrawable, null);
        }

        setBackground(background);
    }

    private void initColor(Context context, ColorStateList color) {
        if (color != null) {
            setColor(color);
        } else {
            // If the user didn't specify any color, try to resolve the current theme's accent color.
            setColor(ColorPalette.resolveAccentColor(context));
        }
    }

    @Override
    public void setElevation(float elevation) {
        if (mElevationDelegate != null) {
            mElevationDelegate.setElevation(elevation);
        } else {
            super.setElevation(elevation);
        }
    }

    @Override
    public float getElevation() {
        if (mElevationDelegate != null) {
            return mElevationDelegate.getElevation();
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
        }
    }

    private void internalSetIsMini(boolean isMini) {
        mIsMini = isMini;
        mRadius = getResources().getDimensionPixelOffset(isMini ? R.dimen.fab_mini_radius : R.dimen.fab_radius);

        if (mElevationDelegate != null) {
            mElevationDelegate.setCornerRadius(mRadius);
        }

        // Re-set layout params so that width and height are adjusted accordingly.
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            setLayoutParams(params);
        }
    }

    public boolean isMini() {
        return mIsMini;
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
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT
                || params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            throw new IllegalStateException("FloatingActionButton 'android:width' and 'android:height' values must be "
                                                    + "'wrap_content'. Check 'isMini' attribute to manipulate size.");
        }

        params.width = mRadius * 2;
        params.height = mRadius * 2;

        if (mElevationDelegate != null) {
            mElevationDelegate.setLayoutParams(params);
        } else {
            super.setLayoutParams(params);
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
    public void superSetLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }
}

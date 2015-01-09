package io.doist.material.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.doist.material.R;
import io.doist.material.color.ColorPalette;
import io.doist.material.drawable.RippleDrawableSimpleCompat;
import io.doist.material.drawable.TintDrawable;

public class FloatingActionButton extends ImageButton {
    private ColorStateList mColor;

    private boolean mIsMini;
    private int mRadius;

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

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        initDrawables(context);
        setFocusable(true);
        setClickable(true);

        // Parse attributes.
        // Default attr values.
        boolean isMini = false;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
            try {
                // Resolve color or colorStateList.
                TypedValue v = new TypedValue();
                if (ta.getValue(R.styleable.FloatingActionButton_android_color, v)) {
                    if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT
                            && v.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        setColor(v.data);
                    } else {
                        setColor(ta.getColorStateList(R.styleable.FloatingActionButton_android_color));
                    }
                }

                // Resolve size.
                isMini = ta.getBoolean(R.styleable.FloatingActionButton_isMini, false);
            } finally {
                ta.recycle();
            }
        }

        if (mColor == null) {
            // If the user didn't specify any color, try to resolve the current theme's accent color.
            setColor(ColorPalette.resolveAccentColor(context));
        }

        setIsMiniInner(isMini);
    }

    private void initDrawables(Context context) {
        // TODO: Make this dependent on the context theme.
        int rippleColorResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                               R.color.ripple_material_light :
                               R.color.ripple_material_light_compat;
        ColorStateList rippleColor = ColorStateList.valueOf(context.getResources().getColor(rippleColorResId));

        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);

        Drawable background;
        // Add ripple to background in Lollipop and newer androids.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            background = new RippleDrawable(rippleColor, circleDrawable, null);
        } else {
            background = new RippleDrawableSimpleCompat(
                    context,
                    rippleColor,
                    new TintDrawable(getContext(), circleDrawable));
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

    public void setColor(ColorStateList color) {
        mColor = color;
        Drawable circleDrawable = ((LayerDrawable)getBackground()).getDrawable(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((GradientDrawable) circleDrawable).setColor(mColor);
        } else {
            //noinspection RedundantCast
            ((TintDrawable) circleDrawable).setTintList(mColor);
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

    private void setIsMiniInner(boolean isMini) {
        mIsMini = isMini;
        int radiusResId = isMini ? R.dimen.fab_mini_radius : R.dimen.fab_radius;
        mRadius = getContext().getResources().getDimensionPixelOffset(radiusResId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mRadius * 2, mRadius * 2);
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
}

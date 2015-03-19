package io.doist.material.drawable;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;

import io.doist.material.R;

/**
 * Used to replace {@link android.graphics.drawable.RippleDrawable} in older androids, with a color animation.
 */
public class RippleMaterialDrawable extends LayerMaterialDrawable {
    ColorStateList mColor;

    Animator mAnimator;

    public RippleMaterialDrawable(Context context) {
        this(context, null);
    }

    public RippleMaterialDrawable(Context context, ColorStateList color) {
        super(context);
        init();
        if (color != null) {
            mColor = color;
        } else {
            TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.colorControlHighlight});
            mColor = ta.getColorStateList(0);
            ta.recycle();

            if (mColor == null) {
                // Fallback color. TODO: make this color depend on the theme.
                mColor = ColorStateList.valueOf(context.getResources().getColor(R.color.ripple_material_light));
            }
        }
    }

    /**
     * Creates a new color animation with the specified color and optional content.
     *
     * @param color   The color to animate
     * @param content The content drawable, may be {@code null}
     */
    public RippleMaterialDrawable(Context context, ColorStateList color, Drawable content) {
        super(context,
              new Drawable[]{
                      content,
                      new TintDrawable(context, content.getConstantState().newDrawable().mutate(), color)});
        init();
    }

    RippleMaterialDrawable(LayerMaterialState state, Resources res) {
        super(state, res);
        init();
    }

    private void init() {
        setPaddingMode(PADDING_MODE_STACK);
    }

    @Override
    protected void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        super.addLayer(layer, id, left, top, right, bottom);
        super.addLayer(
                new TintDrawable(mContext.get(), layer.getConstantState().newDrawable().mutate(), mColor),
                0,
                left,
                top,
                right,
                bottom);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean changed = super.setState(stateSet);

        boolean pressed = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_pressed) {
                pressed = true;
                break;
            }
        }

        if (pressed) {
            startAnimation();
        } else {
            cancelAnimation();
        }

        return changed;
    }

    private void startAnimation() {
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofInt(getDrawable(1), "alpha", 50, 255);
            mAnimator.setInterpolator(new AccelerateInterpolator());
            mAnimator.setDuration(ViewConfiguration.getLongPressTimeout());
        } else {
            mAnimator.setTarget(getDrawable(1));
        }
        mAnimator.start();
    }

    private void cancelAnimation() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        Drawable d = getDrawable(1);
        if (d != null) {
            d.setAlpha(0);
        }
    }

    @Override
    LayerMaterialState createConstantState(LayerMaterialState state) {
        return new RippleState(state);
    }

    static class RippleState extends LayerMaterialState {

        RippleState(LayerMaterialState state) {
            super(state);
        }

        @Override
        public Drawable newDrawable() {
            return new RippleMaterialDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new RippleMaterialDrawable(this, res);
        }

        @Override
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return new RippleMaterialDrawable(this, res);
        }
    }
}

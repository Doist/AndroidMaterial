package io.doist.material.drawable;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;

import io.doist.material.R;

/**
 * Used to replace {@link android.graphics.drawable.RippleDrawable} in older androids, with a color animation.
 */
public class RippleMaterialDrawable extends LayerMaterialDrawable {
    ColorStateList mColor;

    Animator mAnimator;

    RippleMaterialDrawable(Context context) {
        this(context, null);
    }

    RippleMaterialDrawable(Context context, ColorStateList color) {
        super(context);

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
     * @param mask The mask drawable, may be {@code null}
     */
    public RippleMaterialDrawable(Context context, ColorStateList color, @Nullable Drawable content,
                                  @Nullable Drawable mask) {
        super(context, resolveLayers(context, color, content, mask));
        setRippleIndex(getNumberOfLayers() - 1);
    }

    static Drawable[] resolveLayers(Context context, ColorStateList color, Drawable content, Drawable mask) {
        if (content != null && mask != null) {
            return new Drawable[]{
                    content,
                    new TintDrawable(context, mask.getConstantState().newDrawable().mutate(), color)};
        } else if (content != null) {
            return new Drawable[]{
                    content,
                    new TintDrawable(context, content.getConstantState().newDrawable().mutate(), color)};
        } else if (mask != null) {
            return new Drawable[]{
                    new TintDrawable(context, mask.getConstantState().newDrawable().mutate(), color)};
        } else {
            return new Drawable[0];
        }
    }

    RippleMaterialDrawable(LayerMaterialState state, Resources res) {
        super(state, res);
    }

    @Override
    protected void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        if (id != R.id.mask) {
            super.addLayer(layer, id, left, top, right, bottom);
        }

        // If ripple drawable is not yet added.
        if (!hasRipple()) {
            setRippleIndex(getNumberOfLayers());
            super.addLayer(
                    new TintDrawable(mContext.get(), layer.getConstantState().newDrawable().mutate(), mColor),
                    0,
                    left,
                    top,
                    right,
                    bottom);
        }
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
        if (hasRipple()) {
            Drawable rippleDrawable = getDrawable(getRippleIndex());
            if (mAnimator == null) {
                mAnimator = ObjectAnimator.ofInt(rippleDrawable, "alpha", 50, 255);
                mAnimator.setInterpolator(new AccelerateInterpolator());
                mAnimator.setDuration(ViewConfiguration.getLongPressTimeout());
            } else {
                mAnimator.setTarget(rippleDrawable);
            }
            mAnimator.start();
        }
    }

    private void cancelAnimation() {
        if (hasRipple()) {
            if (mAnimator != null) {
                mAnimator.cancel();
            }

            Drawable d = getDrawable(getRippleIndex());
            if (d != null) {
                d.setAlpha(0);
            }
        }
    }

    private void setRippleIndex(int index) {
        ((RippleState) mLayerMaterialState).mRippleIndex = index;
    }

    private int getRippleIndex() {
        return ((RippleState) mLayerMaterialState).mRippleIndex;
    }

    private boolean hasRipple() {
        return getRippleIndex() != -1;
    }

    @Override
    LayerMaterialState createConstantState(LayerMaterialState state) {
        return new RippleState(state);
    }

    static class RippleState extends LayerMaterialState {
        private int mRippleIndex;

        RippleState(LayerMaterialState state) {
            super(state);

            // Force padding default to STACK.
            setPaddingMode(PADDING_MODE_STACK);

            if (state instanceof RippleState) {
                mRippleIndex = ((RippleState) state).mRippleIndex;
            } else {
                mRippleIndex = -1;
            }
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

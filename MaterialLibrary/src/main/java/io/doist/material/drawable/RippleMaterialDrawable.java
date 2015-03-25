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
import android.view.animation.DecelerateInterpolator;

import io.doist.material.R;

/**
 * Used to replace {@link android.graphics.drawable.RippleDrawable} in older androids, with a color animation.
 */
public class RippleMaterialDrawable extends LayerMaterialDrawable {
    ColorStateList mColor;

    Animator mAnimator;
    Drawable mAnimatorTarget;

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
     * @param mask    The mask drawable, may be {@code null}
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
                    createRippleDrawable(context, color, mask)};
        } else if (content != null) {
            return new Drawable[]{
                    content,
                    createRippleDrawable(context, color, content)};
        } else if (mask != null) {
            return new Drawable[]{
                    createRippleDrawable(context, color, mask)};
        } else {
            return new Drawable[0];
        }
    }

    static Drawable createRippleDrawable(Context context, ColorStateList color, Drawable content) {
        return new TintDrawable(context, content.getConstantState().newDrawable().mutate(), color);
    }

    RippleMaterialDrawable(LayerMaterialState state, Resources res) {
        super(state, res);
    }

    @Override
    protected void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        if (id != android.R.id.mask) {
            super.addLayer(layer, id, left, top, right, bottom);
        }

        // If ripple drawable is not yet added.
        if (((RippleState) mLayerMaterialState).mRippleIndex == -1) {
            setRippleIndex(getNumberOfLayers());
            super.addLayer(createRippleDrawable(mContext.get(), mColor, layer), 0, left, top, right, bottom);
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean changed = super.onStateChange(stateSet);

        boolean pressed = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_pressed) {
                pressed = true;
                break;
            }
        }

        if (pressed) {
            startRippleAnimation();
        } else {
            cancelRippleAnimation();
        }

        return changed;
    }

    private void startRippleAnimation() {
        Drawable rippleDrawable = getDrawableSafe(((RippleState) mLayerMaterialState).mRippleIndex);
        if (rippleDrawable != null) {
            if (mAnimator == null) {
                mAnimator = ObjectAnimator.ofInt(null, "alpha", 0, 255);
                mAnimator.setInterpolator(new DecelerateInterpolator());
                mAnimator.setDuration(ViewConfiguration.getLongPressTimeout());
            }
            if (mAnimatorTarget == null) {
                mAnimatorTarget = rippleDrawable;
                mAnimator.setTarget(rippleDrawable);
            }
            if (!mAnimator.isStarted()) {
                mAnimator.start();
            }
        }
    }

    private void cancelRippleAnimation() {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        Drawable rippleDrawable = getDrawableSafe(((RippleState) mLayerMaterialState).mRippleIndex);
        if (rippleDrawable != null) {
            rippleDrawable.setAlpha(0);
        }
    }

    private void setRippleIndex(int index) {
        if (mAnimator != null && mAnimatorTarget != getDrawableSafe(index)) {
            if (mAnimator.isStarted()) {
                cancelRippleAnimation();
            }
            mAnimatorTarget = null;
        }
        ((RippleState) mLayerMaterialState).mRippleIndex = index;
    }

    public Drawable getDrawableSafe(int index) {
        return index >= 0 && index < getNumberOfLayers() ? super.getDrawable(index) : null;
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

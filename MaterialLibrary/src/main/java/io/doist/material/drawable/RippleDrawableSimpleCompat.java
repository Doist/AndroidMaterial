package io.doist.material.drawable;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;

/**
 * Used to replace {@link android.graphics.drawable.RippleDrawable} in older androids, with a color animation.
 */
public class RippleDrawableSimpleCompat extends LayerDrawable {
    Animator mAnimator;

    /**
     * Creates a new color animation with the specified color and optional content.
     *
     * @param color   The color to animate
     * @param content The content drawable, may be {@code null}
     */
    public RippleDrawableSimpleCompat(Context context, ColorStateList color, Drawable content) {
        super(new Drawable[]{
                content,
                new TintDrawable(context, content.getConstantState().newDrawable().mutate(), color)});
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
}

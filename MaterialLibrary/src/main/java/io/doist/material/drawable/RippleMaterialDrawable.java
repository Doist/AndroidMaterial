package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import java.io.IOException;

import io.doist.material.R;

/**
 * Used to replace {@link android.graphics.drawable.RippleDrawable} in older androids, with a color animation.
 */
public class RippleMaterialDrawable extends LayerMaterialDrawable {
    ColorStateList mColor;

    int mAnimationDuration = ViewConfiguration.getLongPressTimeout();

    ValueAnimator mShowRippleAnimator;
    Drawable mShowAnimatorTarget;
    ValueAnimator mHideRippleAnimator;
    Drawable mHideAnimatorTarget;

    boolean mPressed = false;

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
        setRippleIndex(((RippleState) mLayerMaterialState).mRippleIndex);
    }

    @Override
    protected void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        if (id != android.R.id.mask) {
            super.addLayer(layer, id, left, top, right, bottom);
        }

        // If ripple drawable is not yet added.
        if (((RippleState) mLayerMaterialState).mRippleIndex == -1) {
            super.addLayer(createRippleDrawable(mContext.get(), mColor, layer), 0, left, top, right, bottom);
            setRippleIndex(getNumberOfLayers() - 1);
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    public void setColor(ColorStateList color) {
        mColor = color;
        invalidateSelf();
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        // Get attribute values from context instead of resources, so that we can use theme attributes.
        TypedArray a = mContext.get().obtainStyledAttributes(attrs, R.styleable.RippleDrawable);

        // Initialize color.
        final ColorStateList color = a.getColorStateList(R.styleable.RippleDrawable_android_color);
        if (color != null) {
            mColor = color;
        }

        a.recycle();

        // Force padding default to STACK before inflating.
        setPaddingMode(PADDING_MODE_STACK);

        super.inflate(r, parser, attrs);
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

        if (mPressed != pressed) {
            mPressed = pressed;
            if (pressed) {
                startShowRippleAnimation();
            } else {
                startHideRippleAnimation();
            }
        }

        return changed;
    }

    private void startShowRippleAnimation() {
        if (mHideRippleAnimator != null && mHideRippleAnimator.isStarted()) {
            mHideRippleAnimator.cancel();
        }

        Drawable rippleDrawable = getDrawableSafe(((RippleState) mLayerMaterialState).mRippleIndex);
        if (rippleDrawable != null) {
            if (mShowRippleAnimator == null) {
                mShowRippleAnimator = ObjectAnimator.ofInt(null, "alpha", 0, 255);
                mShowRippleAnimator.setInterpolator(new DecelerateInterpolator());
                mShowRippleAnimator.setDuration(mAnimationDuration);
            }
            if (mShowAnimatorTarget == null) {
                mShowAnimatorTarget = rippleDrawable;
                mShowRippleAnimator.setTarget(rippleDrawable);
            }
            if (!mShowRippleAnimator.isStarted()) {
                mShowRippleAnimator.start();
            }
        }
    }

    private void startHideRippleAnimation() {
        long currentTime;
        if (mShowRippleAnimator != null && mShowRippleAnimator.isStarted()) {
            currentTime = Math.min(mShowRippleAnimator.getCurrentPlayTime(), mAnimationDuration / 2);
            mShowRippleAnimator.cancel();
        } else {
            currentTime = mAnimationDuration / 2;
        }

        Drawable rippleDrawable = getDrawableSafe(((RippleState) mLayerMaterialState).mRippleIndex);
        if (rippleDrawable != null) {
            if (mHideRippleAnimator == null) {
                mHideRippleAnimator = ObjectAnimator.ofInt(null, "alpha", 0, 160, 0);
                mHideRippleAnimator.setInterpolator(new DecelerateInterpolator());
                mHideRippleAnimator.setDuration(mAnimationDuration);
            }
            if (mHideAnimatorTarget == null) {
                mHideAnimatorTarget = rippleDrawable;
                mHideRippleAnimator.setTarget(rippleDrawable);
            }
            if (!mHideRippleAnimator.isStarted()) {
                mHideRippleAnimator.start();
                mHideRippleAnimator.setCurrentPlayTime(currentTime);
            }
        }
    }

    private void setRippleIndex(int index) {
        Drawable drawable = getDrawableSafe(index);
        if (mShowRippleAnimator != null && mShowAnimatorTarget != drawable) {
            if (mShowRippleAnimator.isStarted()) {
                startHideRippleAnimation();
            }
            mShowAnimatorTarget = null;
        }
        if (mHideAnimatorTarget != null && mHideAnimatorTarget != drawable) {
            mHideAnimatorTarget = null;
        }
        if (drawable != null) {
            drawable.setAlpha(0); // Init ripple drawable with alpha 0.
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

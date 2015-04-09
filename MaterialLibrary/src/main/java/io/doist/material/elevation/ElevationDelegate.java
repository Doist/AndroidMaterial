package io.doist.material.elevation;

import android.annotation.TargetApi;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import io.doist.material.R;

/**
 * Handles elevation drop shadows for a given view, similar to {@link View#setElevation(float)} on
 * {@link Build.VERSION_CODES#LOLLIPOP} and above.
 *
 * Setup via {@link AttributeSet} or {@link #setElevation(float)}, {@link #setCornerRadius(float)} and
 * {@link #setShownShadows(boolean, boolean, boolean, boolean)}.
 *
 * Your {@link View} must implement {@link Host} and proxy calls to {@link View#setBackground(Drawable)} to
 * {@link #setBackground(Drawable)} and calls to {@link View#setPadding(int, int, int, int)} to
 * {@link #setPadding(int, int, int, int)}. Ideally, calls to {@link View#setLayoutParams(ViewGroup.LayoutParams)} are
 * also proxied to {@link #setLayoutParams(ViewGroup.LayoutParams)}. Optionally, calls to {@link View#getPaddingLeft()}
 * (and others) are proxied to {@link #getUnshadowedPaddingLeft()}. Note that some of these methods are called by the
 * {@link View}'s super constructor, so its {@link ElevationDelegate} will be null at that stage, so check for it.
 * It does the necessary setup upon creation.
 *
 * Caveats:
 * - {@link View#getPaddingLeft()} and others include the shadow padding, so if it used for calculations and re-set
 * on the view via the proxied {@link #setPadding(int, int, int, int)} there will be double margin. To avoid this, add
 * methods in your View and proxy them to {@link #getUnshadowedPaddingLeft()} and others.
 * - Shadowed {@link ViewGroup.LayoutParams#MATCH_PARENT} views will be clipped by their parent. Use
 * {@link android.R.attr#clipChildren} explicitly or, if it won't be visible, hide the side.
 */
public class ElevationDelegate<T extends View & ElevationDelegate.Host> {
    private static final int SHOW_SHADOW_LEFT = 0x01;
    private static final int SHOW_SHADOW_TOP = 0x02;
    private static final int SHOW_SHADOW_RIGHT = 0x04;
    private static final int SHOW_SHADOW_BOTTOM = 0x08;
    private static final int SHOW_ALL_SHADOWS = 0xff;

    private T mView;
    private float mElevation = 0f;
    private float mCornerRadius = 0f;
    private boolean mShowShadowLeft = true;
    private boolean mShowShadowTop = true;
    private boolean mShowShadowRight = true;
    private boolean mShowShadowBottom = true;

    public ElevationDelegate(T view) {
        this(view, null, 0);
    }

    /**
     * Creates and returns a configured {@link ElevationDelegate}. Initial setup of the {@link View}, such as wrapping
     * its background inside a {@link ElevationWrapperDrawable}, is done automatically.
     */
    public ElevationDelegate(T view, AttributeSet attrs, int defStyleAttr) {
        mView = view;

        // Parse the attrs, if any.
        if (attrs != null) {
            TypedArray a =
                    view.getContext().obtainStyledAttributes(attrs, R.styleable.ElevationDelegate, defStyleAttr, 0);
            mElevation = a.getDimensionPixelOffset(R.styleable.ElevationDelegate_elevation, 0);
            mCornerRadius = a.getDimensionPixelOffset(R.styleable.ElevationDelegate_cornerRadius, 0);
            int showShadows = a.getInt(R.styleable.ElevationDelegate_showShadows, SHOW_ALL_SHADOWS);
            mShowShadowLeft = (showShadows & SHOW_SHADOW_LEFT) == SHOW_SHADOW_LEFT;
            mShowShadowTop = (showShadows & SHOW_SHADOW_TOP) == SHOW_SHADOW_TOP;
            mShowShadowRight = (showShadows & SHOW_SHADOW_RIGHT) == SHOW_SHADOW_RIGHT;
            mShowShadowBottom = (showShadows & SHOW_SHADOW_BOTTOM) == SHOW_SHADOW_BOTTOM;
            a.recycle();
        }

        // Ensure the background set on the view is a ShadowDrawableWrapper.
        Drawable background = mView.getBackground();
        if (background != null && !(background instanceof ElevationWrapperDrawable)) {
            mView.setBackground(null); // Removes the callback.
            setBackground(background);
        }
    }

    /**
     * @see {@link View#getElevation()}.
     */
    public float getElevation() {
        return mElevation;
    }

    /**
     * @see {@link View#setElevation(float)}.
     */
    public void setElevation(float elevation) {
        mElevation = elevation;

        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            int unshadowedPaddingLeft = getUnshadowedPaddingLeft();
            int unshadowedPaddingTop = getUnshadowedPaddingTop();
            int unshadowedPaddingRight = getUnshadowedPaddingRight();
            int unshadowedPaddingBottom = getUnshadowedPaddingBottom();

            elevationWrapperDrawable.setElevation(elevation);

            setPadding(unshadowedPaddingLeft, unshadowedPaddingTop, unshadowedPaddingRight, unshadowedPaddingBottom);
        }
    }

    /**
     * Sets the corner radius for the elevation shadow.
     * For regular backgrounds, it's 0. For circular backgrounds, it's the same as the width and height.
     */
    public void setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;

        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            elevationWrapperDrawable.setCornerRadius(cornerRadius);
        }
    }

    /**
     * Sets the edges where the shadow will be drawn. The edges will be padded.
     */
    public void setShownShadows(boolean left, boolean top, boolean right, boolean bottom) {
        mShowShadowLeft = left;
        mShowShadowTop = top;
        mShowShadowRight = right;
        mShowShadowBottom = bottom;

        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            int unshadowedPaddingLeft = getUnshadowedPaddingLeft();
            int unshadowedPaddingTop = getUnshadowedPaddingTop();
            int unshadowedPaddingRight = getUnshadowedPaddingRight();
            int unshadowedPaddingBottom = getUnshadowedPaddingBottom();

            elevationWrapperDrawable.setShownShadows(mShowShadowLeft, mShowShadowTop,
                                                  mShowShadowRight, mShowShadowBottom);

            setPadding(unshadowedPaddingLeft, unshadowedPaddingTop, unshadowedPaddingRight, unshadowedPaddingBottom);
        }
    }

    /**
     * Wraps {@code background} inside a {@link ElevationWrapperDrawable} so that a shadow is drawn around it.
     * The3 needed padding is added automatically.
     *
     * Proxy {@link View#setBackground(Drawable)} calls here.
     */
    public void setBackground(Drawable background) {
        int unshadowedPaddingLeft = getUnshadowedPaddingLeft();
        int unshadowedPaddingTop = getUnshadowedPaddingTop();
        int unshadowedPaddingRight = getUnshadowedPaddingRight();
        int unshadowedPaddingBottom = getUnshadowedPaddingBottom();

        mView.superSetBackground(new ElevationWrapperDrawable(background, mView, mElevation, mCornerRadius,
                                                              mShowShadowLeft, mShowShadowTop,
                                                              mShowShadowRight, mShowShadowBottom));

        setPadding(unshadowedPaddingLeft, unshadowedPaddingTop, unshadowedPaddingRight, unshadowedPaddingBottom);
    }

    /**
     * Adjusts the padding to account for the extra padding needed for the shadow and sets it in the {@link View}.
     *
     * Proxy {@link View#setPadding(int, int, int, int)} calls here.
     *
     * @see #getUnshadowedPaddingLeft()
     * @see #getUnshadowedPaddingTop()
     * @see #getUnshadowedPaddingRight()
     * @see #getUnshadowedPaddingBottom()
     */
    public void setPadding(int left, int top, int right, int bottom) {
        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            left += elevationWrapperDrawable.getPaddingLeft();
            top += elevationWrapperDrawable.getPaddingTop();
            right += elevationWrapperDrawable.getPaddingRight();
            bottom += elevationWrapperDrawable.getPaddingBottom();
        }
        mView.superSetPadding(left, top, right, bottom);
    }

    /**
     * Adjusts the padding to account for the extra padding needed for the shadow and sets it in the {@link View}.
     *
     * Proxy {@link View#setPaddingRelative(int, int, int, int)} calls here.
     *
     * @see #getUnshadowedPaddingStart()
     * @see #getUnshadowedPaddingTop()
     * @see #getUnshadowedPaddingEnd()
     * @see #getUnshadowedPaddingBottom()
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        boolean rtl = mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        setPadding(rtl ? end : start, top, rtl ? start : end, bottom);
    }

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        ElevationWrapperDrawable elevationWrapperDrawable;
        if (layoutParams != null && (elevationWrapperDrawable = getShadowDrawableWrapper()) != null) {
            int paddingLeft = elevationWrapperDrawable.getPaddingLeft();
            int paddingTop = elevationWrapperDrawable.getPaddingTop();
            int paddingRight = elevationWrapperDrawable.getPaddingRight();
            int paddingBottom = elevationWrapperDrawable.getPaddingBottom();

            if (layoutParams.width > 0) {
                layoutParams.width += paddingLeft + paddingRight;
            }
            if (layoutParams.height > 0) {
                layoutParams.height += paddingTop + paddingBottom;
            }

            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                marginLayoutParams.leftMargin -= paddingLeft;
                marginLayoutParams.topMargin -= paddingTop;
                marginLayoutParams.rightMargin -= paddingRight;
                marginLayoutParams.bottomMargin -= paddingBottom;
            }
        }
        mView.superSetLayoutParams(layoutParams);
    }

    /**
     * Returns the {@link View}'s left padding excluding the extra added for the shadow.
     */
    public int getUnshadowedPaddingLeft() {
        int paddingLeft = mView.getPaddingLeft();
        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            paddingLeft -= elevationWrapperDrawable.getPaddingLeft();
        }
        return paddingLeft;
    }

    /**
     * Returns the {@link View}'s top padding excluding the extra added for the shadow.
     */
    public int getUnshadowedPaddingTop() {
        int paddingTop = mView.getPaddingTop();
        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            paddingTop -= elevationWrapperDrawable.getPaddingTop();
        }
        return paddingTop;
    }

    /**
     * Returns the {@link View}'s right padding excluding the extra added for the shadow.
     */
    public int getUnshadowedPaddingRight() {
        int paddingRight = mView.getPaddingRight();
        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            paddingRight -= elevationWrapperDrawable.getPaddingRight();
        }
        return paddingRight;
    }

    /**
     * Returns the {@link View}'s bottom padding excluding the extra added for the shadow.
     */
    public int getUnshadowedPaddingBottom() {
        int paddingBottom = mView.getPaddingBottom();
        ElevationWrapperDrawable elevationWrapperDrawable = getShadowDrawableWrapper();
        if (elevationWrapperDrawable != null) {
            paddingBottom -= elevationWrapperDrawable.getPaddingBottom();
        }
        return paddingBottom;
    }

    /**
     * Returns the {@link View}'s start padding excluding the extra added for the shadow.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getUnshadowedPaddingStart() {
        return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? getUnshadowedPaddingRight()
                                                                       : getUnshadowedPaddingLeft();
    }

    /**
     * Returns the {@link View}'s end padding excluding the extra added for the shadow.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getUnshadowedPaddingEnd() {
        return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? getUnshadowedPaddingLeft()
                                                                       : getUnshadowedPaddingRight();
    }

    private ElevationWrapperDrawable getShadowDrawableWrapper() {
        Drawable background = mView.getBackground();
        if (background instanceof ElevationWrapperDrawable) {
            return (ElevationWrapperDrawable) background;
        } else {
            return null;
        }
    }

    public interface Host {
        /**
         * Set the background using the {@link View}'s superclass. Used to avoid invocation loops.
         */
        void superSetBackground(Drawable background);

        /**
         * Set the padding using the {@link View}'s superclass. Used to avoid invocation loops.
         */
        void superSetPadding(int left, int top, int right, int bottom);

        /**
         * Set the layout params using the {@link View}'s superclass. Used to avoid invocation loops.
         */
        void superSetLayoutParams(ViewGroup.LayoutParams params);
    }
}

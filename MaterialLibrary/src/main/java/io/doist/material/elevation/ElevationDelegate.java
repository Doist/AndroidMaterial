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
 * Create via {@link AttributeSet} or {@link #setElevation(float)}, {@link #setCornerRadius(float)} and
 * {@link #setShownShadows(boolean, boolean, boolean, boolean)}.
 *
 * The elevated {@link View} must proxy {@link View#onAttachedToWindow()} and {@link View#onDetachedFromWindow()} calls
 * to {@link #onAttachedToWindow()} and {@link #onDetachedFromWindow()}, respectively, so that its background is wrapped
 * and its padding / size / margin adjusted accordingly.
 *
 * Note that after {@link #onAttachedToWindow()}, the {@link View} will have modified background, padding, size
 * (if absolute dimensions are used) and margins. This is fine for most scenarios (most adjustments at runtime are
 * relative, ie. {@code getPaddingLeft() + N}), but if the need arises to access the originals there are methods to do
 * so. See:
 * - {@link #getOriginalBackground()}
 * - {@link #getOriginalPaddingLeft()} (plus all others variants)
 * - {@link #getOriginalLayoutParams()}
 * Worst case scenario, {@link #onDetachedFromWindow()} can be called to make modifications based on the original state,
 * followed by {@link #onAttachedToWindow()}.
 *
 * Caveat:
 * - Elevated {@link ViewGroup.LayoutParams#MATCH_PARENT} views will be clipped by their parent. Use
 * {@link android.R.attr#clipChildren} explicitly or, if it won't be visible, hide the side (preferred).
 */
public class ElevationDelegate {
    private static final int SHOW_SHADOW_LEFT = 0x01;
    private static final int SHOW_SHADOW_TOP = 0x02;
    private static final int SHOW_SHADOW_RIGHT = 0x04;
    private static final int SHOW_SHADOW_BOTTOM = 0x08;
    private static final int SHOW_ALL_SHADOWS = 0xff;

    private View mView;
    private float mElevation = 0f;
    private float mCornerRadius = 0f;
    private boolean mShowShadowLeft = true;
    private boolean mShowShadowTop = true;
    private boolean mShowShadowRight = true;
    private boolean mShowShadowBottom = true;

    public ElevationDelegate(View view) {
        this(view, null, 0);
    }

    /**
     * Creates and returns a configured {@link ElevationDelegate}. Initial setup of the {@link View}, such as wrapping
     * its background inside a {@link ElevationWrapperDrawable}, is done automatically.
     */
    public ElevationDelegate(View view, AttributeSet attrs, int defStyleAttr) {
        mView = view;

        // Parse the attrs, if any.
        if (attrs != null) {
            TypedArray a =
                    view.getContext().obtainStyledAttributes(attrs, R.styleable.ElevationDelegate, defStyleAttr, 0);
            mElevation = a.getDimensionPixelOffset(R.styleable.ElevationDelegate_elevation, 0);
            mCornerRadius = a.getDimensionPixelOffset(R.styleable.ElevationDelegate_cornerRadius, 0);
            int shownShadows = a.getInt(R.styleable.ElevationDelegate_shownShadows, SHOW_ALL_SHADOWS);
            mShowShadowLeft = (shownShadows & SHOW_SHADOW_LEFT) == SHOW_SHADOW_LEFT;
            mShowShadowTop = (shownShadows & SHOW_SHADOW_TOP) == SHOW_SHADOW_TOP;
            mShowShadowRight = (shownShadows & SHOW_SHADOW_RIGHT) == SHOW_SHADOW_RIGHT;
            mShowShadowBottom = (shownShadows & SHOW_SHADOW_BOTTOM) == SHOW_SHADOW_BOTTOM;
            a.recycle();
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
        boolean needsWrap = elevation > mElevation;
        mElevation = elevation;

        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            if (needsWrap) {
                unwrap();
            }
            elevationDrawable.setElevation(elevation);
            if (needsWrap) {
                wrap();
            }
        }
    }

    /**
     * Sets the corner radius for the elevation shadow.
     * For regular backgrounds, it's 0. For circular backgrounds, it's the same as the width and height.
     */
    public void setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;

        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            elevationDrawable.setCornerRadius(cornerRadius);
        }
    }

    /**
     * Sets the edges where the shadow will be drawn.
     */
    public void setShownShadows(boolean left, boolean top, boolean right, boolean bottom) {
        boolean needsWrap = left && !mShowShadowLeft || top && !mShowShadowTop
                || right && !mShowShadowRight || bottom && !mShowShadowBottom;
        mShowShadowLeft = left;
        mShowShadowTop = top;
        mShowShadowRight = right;
        mShowShadowBottom = bottom;

        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            if (needsWrap) {
                unwrap();
            }
            elevationDrawable.setShownShadows(left, top, right, bottom);
            if (needsWrap) {
                wrap();
            }
        }
    }

    /**
     * Wraps {@link View}'s background in an {@link ElevationWrapperDrawable} and adjusts its padding, size and margins.
     */
    public void onAttachedToWindow() {
        wrap();
    }

    /**
     * Unwraps the {@link View}'s background, resetting the original padding, size and margins.
     */
    public void onDetachedFromWindow() {
        unwrap();
    }

    /**
     * Returns the {@link View}'s background excluding the wrapper elevation background.
     */
    public Drawable getOriginalBackground() {
        Drawable background = mView.getBackground();
        if (background instanceof ElevationWrapperDrawable) {
            background = ((ElevationWrapperDrawable) background).getWrappedDrawable();
        }
        return background;
    }

    /**
     * Returns the {@link View}'s left padding excluding the extra added for the shadow.
     */
    public int getOriginalPaddingLeft() {
        int paddingLeft = mView.getPaddingLeft();
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            paddingLeft -= elevationDrawable.getPaddingLeft();
        }
        return paddingLeft;
    }

    /**
     * Returns the {@link View}'s top padding excluding the extra added for the shadow.
     */
    public int getOriginalPaddingTop() {
        int paddingTop = mView.getPaddingTop();
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            paddingTop -= elevationDrawable.getPaddingTop();
        }
        return paddingTop;
    }

    /**
     * Returns the {@link View}'s right padding excluding the extra added for the shadow.
     */
    public int getOriginalPaddingRight() {
        int paddingRight = mView.getPaddingRight();
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            paddingRight -= elevationDrawable.getPaddingRight();
        }
        return paddingRight;
    }

    /**
     * Returns the {@link View}'s bottom padding excluding the extra added for the shadow.
     */
    public int getOriginalPaddingBottom() {
        int paddingBottom = mView.getPaddingBottom();
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            paddingBottom -= elevationDrawable.getPaddingBottom();
        }
        return paddingBottom;
    }

    /**
     * Returns the {@link View}'s start padding excluding the extra added for the shadow.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getOriginalPaddingStart() {
        return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? getOriginalPaddingRight()
                                                                       : getOriginalPaddingLeft();
    }

    /**
     * Returns the {@link View}'s end padding excluding the extra added for the shadow.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getOriginalPaddingEnd() {
        return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? getOriginalPaddingLeft()
                                                                       : getOriginalPaddingRight();
    }

    /**
     * Returns the left padding added by {@link ElevationDelegate} to display the shadow.
     */
    public int getElevationPaddingLeft() {
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            return elevationDrawable.getPaddingLeft();
        } else {
            return 0;
        }
    }

    /**
     * Returns the top padding added by {@link ElevationDelegate} to display the shadow.
     */
    public int getElevationPaddingTop() {
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            return elevationDrawable.getPaddingTop();
        } else {
            return 0;
        }
    }

    /**
     * Returns the right padding added by {@link ElevationDelegate} to display the shadow.
     */
    public int getElevationPaddingRight() {
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            return elevationDrawable.getPaddingRight();
        } else {
            return 0;
        }
    }

    /**
     * Returns the bottom padding added by {@link ElevationDelegate} to display the shadow.
     */
    public int getElevationPaddingBottom() {
        ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
        if (elevationDrawable != null) {
            return elevationDrawable.getPaddingBottom();
        } else {
            return 0;
        }
    }

    /**
     * Returns the {@link View}'s start padding excluding the extra added for the shadow.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getElevationPaddingStart() {
        return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? getElevationPaddingRight()
                                                                       : getElevationPaddingLeft();
    }

    /**
     * Returns the {@link View}'s end padding excluding the extra added for the shadow.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getElevationPaddingEnd() {
        return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? getElevationPaddingLeft()
                                                                       : getElevationPaddingRight();
    }

    /**
     * Returns a copy of the {@link View}'s {@link ViewGroup.LayoutParams} excluding the size / margin modifications.
     */
    public ViewGroup.LayoutParams getOriginalLayoutParams() {
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        if (params != null) {
            ElevationWrapperDrawable elevationDrawable = getElevationDrawableWrapper();
            if (elevationDrawable != null) {
                int paddingLeft = elevationDrawable.getPaddingLeft();
                int paddingTop = elevationDrawable.getPaddingTop();
                int paddingRight = elevationDrawable.getPaddingRight();
                int paddingBottom = elevationDrawable.getPaddingBottom();

                ViewGroup.LayoutParams originalParams;
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams originMarginParams =
                            new ViewGroup.MarginLayoutParams((ViewGroup.MarginLayoutParams) params);

                    originMarginParams.leftMargin -= paddingLeft;
                    originMarginParams.topMargin -= paddingTop;
                    originMarginParams.rightMargin -= paddingRight;
                    originMarginParams.bottomMargin -= paddingBottom;

                    originalParams = originMarginParams;
                } else {
                    originalParams = new ViewGroup.LayoutParams(params);
                }

                if (params.width > 0) {
                    originalParams.width -= paddingLeft + paddingRight;
                }
                if (params.height > 0) {
                    originalParams.height -= paddingTop + paddingBottom;
                }

                return originalParams;
            } else {
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    return new ViewGroup.MarginLayoutParams(params);
                } else {
                    return new ViewGroup.LayoutParams(params);
                }
            }
        } else {
            return null;
        }
    }

    private void wrap() {
        Drawable background = mView.getBackground();
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        if (background != null && !(background instanceof ElevationWrapperDrawable) && params != null) {
            // Remove the drawable to avoid the wrapper drawable callback being removed when it's set on the view below.
            mView.setBackground(null);

            ElevationWrapperDrawable elevationDrawable =
                    new ElevationWrapperDrawable(background, mView, mElevation, mCornerRadius,
                                                 mShowShadowLeft, mShowShadowTop,
                                                 mShowShadowRight, mShowShadowBottom);
            // Set elevation wrapper drawable around the background.
            mView.setBackground(elevationDrawable);

            int paddingLeft = elevationDrawable.getPaddingLeft();
            int paddingTop = elevationDrawable.getPaddingTop();
            int paddingRight = elevationDrawable.getPaddingRight();
            int paddingBottom = elevationDrawable.getPaddingBottom();

            // Increment the padding to accommodate the elevation.
            mView.setPadding(mView.getPaddingLeft() + paddingLeft,
                             mView.getPaddingTop() + paddingTop,
                             mView.getPaddingRight() + paddingRight,
                             mView.getPaddingBottom() + paddingBottom);

            // Minimum height.
            int minHeight = mView.getMinimumHeight();
            if (minHeight > 0) {
                mView.setMinimumHeight(minHeight + (paddingTop + paddingBottom));
            }

            // Increase layout size (if using explicit dimensions) and decrease the margins proportionally to padding.
            if (params.width > 0) {
                params.width += paddingLeft + paddingRight;
            }
            if (params.height > 0) {
                params.height += paddingTop + paddingBottom;
            }
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
                marginLayoutParams.leftMargin -= paddingLeft;
                marginLayoutParams.topMargin -= paddingTop;
                marginLayoutParams.rightMargin -= paddingRight;
                marginLayoutParams.bottomMargin -= paddingBottom;
            }
            mView.setLayoutParams(params);
        }
    }

    private void unwrap() {
        Drawable background = mView.getBackground();
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        if (background != null && background instanceof ElevationWrapperDrawable && params != null) {
            ElevationWrapperDrawable elevationDrawable = (ElevationWrapperDrawable) background;

            // Background.
            mView.setBackground(null); // Removes the callback.
            mView.setBackground(elevationDrawable.getWrappedDrawable());

            int paddingLeft = elevationDrawable.getPaddingLeft();
            int paddingTop = elevationDrawable.getPaddingTop();
            int paddingRight = elevationDrawable.getPaddingRight();
            int paddingBottom = elevationDrawable.getPaddingBottom();

            // Padding.
            mView.setPadding(mView.getPaddingLeft() - paddingLeft,
                             mView.getPaddingTop() - paddingTop,
                             mView.getPaddingRight() - paddingRight,
                             mView.getPaddingBottom() - paddingBottom);

            // Minimum height.
            int minHeight = mView.getMinimumHeight();
            if (minHeight > 0) {
                mView.setMinimumHeight(minHeight - (paddingTop + paddingBottom));
            }

            // Layout size and margins.
            if (params.width > 0) {
                params.width -= paddingLeft + paddingRight;
            }
            if (params.height > 0) {
                params.height -= paddingTop + paddingBottom;
            }
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
                marginLayoutParams.leftMargin += paddingLeft;
                marginLayoutParams.topMargin += paddingTop;
                marginLayoutParams.rightMargin += paddingRight;
                marginLayoutParams.bottomMargin += paddingBottom;
            }
            mView.setLayoutParams(params);
        }
    }

    private ElevationWrapperDrawable getElevationDrawableWrapper() {
        Drawable background = mView.getBackground();
        if (background instanceof ElevationWrapperDrawable) {
            return (ElevationWrapperDrawable) background;
        } else {
            return null;
        }
    }
}

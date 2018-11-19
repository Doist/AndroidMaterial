package io.doist.material.elevation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.doist.material.drawable.WrapperDrawable;

/**
 * Wraps a {@link Drawable} and draws an elevation drop shadow around it.
 */
class CompatElevationDrawable extends WrapperDrawable implements CompatElevationUpdateRunnable.ShadowUpdateListener {
    // For calculating each shadow length.
    private static final int LIGHT_HEIGHT_DIP = 800;
    private static final int LIGHT_Y_OFFSET_DIP = 640;
    private static final int LIGHT_X_OFFSET_DIP = 160;

    // For calculating each shadows alpha.
    private static final float AMBIENT_ALPHA = 0.09f;
    private static final float SIDE_ALPHA = 0.14f;
    private static final float MIN_BOTTOM_ALPHA = 0.18f; // Bottom shadow when at top.
    private static final float INC_BOTTOM_ALPHA = 0.05f; // Bottom shadow increment when at the bottom.

    // Executor service for calculating the shadow in the background.
    private static BlockingQueue<Runnable> sShadowExecutorQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor sShadowExecutor =
            new ThreadPoolExecutor(0, 1, 2, TimeUnit.SECONDS, sShadowExecutorQueue);

    private WeakReference<View> mViewRef;
    private float mElevation = 0f;
    private float mCornerRadius = 0f;

    private boolean mShowShadowLeft = true;
    private boolean mShowShadowTop = true;
    private boolean mShowShadowRight = true;
    private boolean mShowShadowBottom = true;

    // Source light / screen.
    private float mLightHeight;
    private float mLightOffsetX;
    private float mLightOffsetY;
    private int mScreenWidth;
    private int mScreenHeight;

    // Edge paints.
    private Paint mShadowPaintLeft;
    private Paint mShadowPaintTop;
    private Paint mShadowPaintRight;
    private Paint mShadowPaintBottom;

    // Corner paint.
    private Paint mCornerPaint;

    // Corner bitmaps.
    private Bitmap mShadowBitmapTopLeft;
    private Bitmap mShadowBitmapTopRight;
    private Bitmap mShadowBitmapBottomRight;
    private Bitmap mShadowBitmapBottomLeft;

    // Shadow lengths in each direction.
    private int mShadowLengthLeft;
    private int mShadowLengthTop;
    private int mShadowLengthRight;
    private int mShadowLengthBottom;

    // Shadow padding in each direction (maximum possible).
    private int mShadowPaddingLeft;
    private int mShadowPaddingTop;
    private int mShadowPaddingRight;
    private int mShadowPaddingBottom;

    // Wrapped drawable padding.
    private Rect mWrappedPadding = new Rect();

    // Avoid allocations.
    private int[] mScreenLocation = new int[2];
    private Rect mBounds = new Rect();
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    // Shadow setup or not.
    private volatile boolean mIsShadowSetup;

    // Handler for invalidating the drawable on the UI thread.
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidateSelf();
        }
    };

    public CompatElevationDrawable(Drawable drawable, View view, float elevation, float cornerRadius,
                                   boolean showShadowLeft, boolean showShadowTop,
                                   boolean showShadowRight, boolean showShadowBottom) {

        super(drawable);

        mViewRef = new WeakReference<>(view);
        mElevation = elevation;
        mCornerRadius = cornerRadius;

        mShowShadowLeft = showShadowLeft;
        mShowShadowTop = showShadowTop;
        mShowShadowRight = showShadowRight;
        mShowShadowBottom = showShadowBottom;

        mLightHeight = dpToPx(LIGHT_HEIGHT_DIP);
        mLightOffsetX = dpToPx(LIGHT_X_OFFSET_DIP);
        mLightOffsetY = dpToPx(LIGHT_Y_OFFSET_DIP);
        DisplayMetrics metrics = view.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mShadowPaintLeft = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mShadowPaintLeft.setStyle(Paint.Style.FILL);
        mShadowPaintTop = new Paint(mShadowPaintLeft);
        mShadowPaintRight = new Paint(mShadowPaintLeft);
        mShadowPaintBottom = new Paint(mShadowPaintLeft);
        mCornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        calculatePadding();

        drawable.getPadding(mWrappedPadding);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);

        mShadowPaintLeft.setAlpha(alpha);
        mShadowPaintTop.setAlpha(alpha);
        mShadowPaintRight.setAlpha(alpha);
        mShadowPaintBottom.setAlpha(alpha);
        mCornerPaint.setAlpha(alpha);
    }

    public void setShownShadows(boolean left, boolean top, boolean right, boolean bottom) {
        if (mShowShadowLeft != left || mShowShadowTop != top
                || mShowShadowRight != right || mShowShadowBottom != bottom) {
            mShowShadowLeft = left;
            mShowShadowTop = top;
            mShowShadowRight = right;
            mShowShadowBottom = bottom;

            // Clear bitmap references, as the necessary ones will be recreated in update().
            mShadowBitmapTopLeft = null;
            mShadowBitmapTopRight = null;
            mShadowBitmapBottomRight = null;
            mShadowBitmapBottomLeft = null;

            calculatePadding();

            update(true, false);
        }
    }

    /**
     * @see CompatElevationDelegate#setElevation(float)
     */
    public void setElevation(float elevation) {
        if (mElevation != elevation) {
            mElevation = elevation;

            calculatePadding();

            update(true, false);
        }
    }

    /**
     * @see CompatElevationDelegate#setCornerRadius(float)
     */
    public void setCornerRadius(float cornerRadius) {
        if (mCornerRadius != cornerRadius) {
            mCornerRadius = cornerRadius;

            update(true, false);
        }
    }

    // Padding is managed by ElevationDelegate, as this would clear the original padding when the background is set.
    @Override
    public boolean getPadding(Rect padding) {
        return false;
    }

    private void calculatePadding() {
        mShadowPaddingLeft = getShadowLengthLeft(0);
        mShadowPaddingTop = getShadowLengthTop();
        mShadowPaddingRight = getShadowLengthRight(mScreenWidth);
        mShadowPaddingBottom = getShadowLengthBottom(mScreenHeight);
    }

    public int getPaddingLeft() {
        return mShowShadowLeft ? mShadowPaddingLeft : 0;
    }

    public int getPaddingTop() {
        return mShowShadowTop ? mShadowPaddingTop : 0;
    }

    public int getPaddingRight() {
        return mShowShadowRight ? mShadowPaddingRight : 0;
    }

    public int getPaddingBottom() {
        return mShowShadowBottom ? mShadowPaddingBottom : 0;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        bounds.left += getPaddingLeft();
        bounds.top += getPaddingTop();
        bounds.right -= getPaddingRight();
        bounds.bottom -= getPaddingBottom();

        getWrappedDrawable().setBounds(bounds);

        mBounds.set(bounds.left + mWrappedPadding.left,
                    bounds.top + mWrappedPadding.top,
                    bounds.right - mWrappedPadding.right,
                    bounds.bottom - mWrappedPadding.bottom);
    }

    @Override
    public void draw(Canvas canvas) {
        drawShadow(canvas);

        super.draw(canvas);
    }

    private void drawShadow(Canvas canvas) {
        // Bail out immediately if there's no elevation.
        if (mElevation == 0f) {
            return;
        }

        // Ensure shadow is up-to-date (in the background, if previously setup).
        update(false, mIsShadowSetup);

        int width = mBounds.width();
        int height = mBounds.height();

        // Translate the canvas to the area that will be drawn.
        int count = canvas.save();
        canvas.translate(getPaddingLeft() + mWrappedPadding.left - mShadowLengthLeft,
                         getPaddingTop() + mWrappedPadding.top - mShadowLengthTop);

        // Draw edges.
        if (mShowShadowLeft) {
            canvas.drawRect(0,
                            mShadowLengthTop + mCornerRadius,
                            mShadowLengthLeft,
                            mShadowLengthTop + height - mCornerRadius,
                            mShadowPaintLeft);
        }
        if (mShowShadowTop) {
            canvas.drawRect(mShadowLengthLeft + mCornerRadius,
                            0,
                            mShadowLengthLeft + width - mCornerRadius,
                            mShadowLengthTop,
                            mShadowPaintTop);
        }
        if (mShowShadowRight) {
            canvas.drawRect(mShadowLengthLeft + width,
                            mShadowLengthTop + mCornerRadius,
                            mShadowLengthLeft + width + mShadowLengthRight,
                            mShadowLengthTop + height - mCornerRadius,
                            mShadowPaintRight);
        }
        if (mShowShadowBottom) {
            canvas.drawRect(mShadowLengthLeft + mCornerRadius,
                            mShadowLengthTop + height,
                            mShadowLengthLeft + width - mCornerRadius,
                            mShadowLengthTop + height + mShadowLengthBottom,
                            mShadowPaintBottom);
        }

        // Draw corners.
        if (mShowShadowLeft && mShowShadowTop) {
            canvas.drawBitmap(mShadowBitmapTopLeft,
                              0,
                              0,
                              mCornerPaint);
        }
        if (mShowShadowTop && mShowShadowRight) {
            canvas.drawBitmap(mShadowBitmapTopRight,
                              mShadowLengthLeft + width - mCornerRadius,
                              0,
                              mCornerPaint);
        }
        if (mShowShadowRight && mShowShadowBottom) {
            canvas.drawBitmap(mShadowBitmapBottomRight,
                              mShadowLengthLeft + width - mCornerRadius,
                              mShadowLengthTop + height - mCornerRadius,
                              mCornerPaint);
        }
        if (mShowShadowBottom && mShowShadowLeft) {
            canvas.drawBitmap(mShadowBitmapBottomLeft,
                              0,
                              mShadowLengthTop + height - mCornerRadius,
                              mCornerPaint);
        }

        // Restore the canvas.
        canvas.restoreToCount(count);
    }

    /**
     * Updates the necessary edge paints and corner bitmaps for the current state.
     *
     * @param force forces the update to go through, even if there's no indication that the shadow has changed.
     * @param async runs the update in the background instead of the calling thread.
     */
    private void update(boolean force, boolean async) {
        View view = mViewRef.get();
        if (view != null && mElevation > 0) {
            view.getLocationOnScreen(mScreenLocation);
            int left = mScreenLocation[0] + getPaddingLeft();
            int top = mScreenLocation[1] + getPaddingTop();
            int right = left + mBounds.width();
            int bottom = top + mBounds.height();

            if (force || left != mLeft || top != mTop || right != mRight || bottom != mBottom) {
                // Check if there were changes in the drawable's dimensions.
                boolean heightChanged = (bottom - top) != (mBottom - mTop);
                boolean widthChanged = (right - left) != (mRight - mLeft);

                mLeft = left;
                mTop = top;
                mRight = right;
                mBottom = bottom;
                
                int shadowLengthLeft = getShadowLengthLeft(left);
                int shadowLengthTop = getShadowLengthTop();
                int shadowLengthRight = getShadowLengthRight(right);
                int shadowLengthBottom = getShadowLengthBottom(bottom);

                boolean leftDirty = mShowShadowLeft && (force || shadowLengthLeft != mShadowLengthLeft || widthChanged);
                boolean topDirty = mShowShadowTop && (force || shadowLengthTop != mShadowLengthTop || heightChanged);
                boolean rightDirty =
                        mShowShadowRight && (force || shadowLengthRight != mShadowLengthRight || widthChanged);
                boolean bottomDirty =
                        mShowShadowBottom && (force || shadowLengthBottom != mShadowLengthBottom || heightChanged);

                if (leftDirty || topDirty || rightDirty || bottomDirty) {
                    float shadowAlphaLeft = shadowLengthLeft > shadowLengthRight ? SIDE_ALPHA : AMBIENT_ALPHA;
                    float shadowAlphaTop = AMBIENT_ALPHA;
                    float shadowAlphaRight = shadowLengthRight > shadowLengthLeft ? SIDE_ALPHA : AMBIENT_ALPHA;
                    float shadowAlphaBottom =
                            MIN_BOTTOM_ALPHA + INC_BOTTOM_ALPHA * shadowLengthBottom / getPaddingBottom();

                    CompatElevationUpdateRunnable runnable =
                            new CompatElevationUpdateRunnable(
                                    mLeft, mTop, mRight, mBottom, mCornerRadius,
                                    shadowLengthLeft, shadowLengthTop, shadowLengthRight, shadowLengthBottom,
                                    shadowAlphaLeft, shadowAlphaTop, shadowAlphaRight, shadowAlphaBottom,
                                    leftDirty, topDirty, rightDirty, bottomDirty, this);
                    if (async) {
                        sShadowExecutor.execute(runnable);
                    } else {
                        runnable.run();
                    }
                }

            }
        }
    }

    /**
     * Returns the ambient shadow for the current elevation.
     */
    private int getShadowLengthAmbient() {
        return (int) Math.ceil(mElevation * 3 / 8);
    }

    /**
     * Returns the left shadow length for {@code left} position.
     */
    private int getShadowLengthLeft(int left) {
        int shadowLengthAmbient = getShadowLengthAmbient();
        if (left < mScreenWidth / 2f) {
            return Math.max(shadowLengthAmbient,
                            (int) (mElevation / (mLightHeight / (mLightOffsetX + (mScreenWidth - left)))));
        } else {
            return shadowLengthAmbient;
        }
    }

    /**
     * Returns the top shadow length.
     */
    private int getShadowLengthTop() {
        return getShadowLengthAmbient();
    }

    /**
     * Returns the right shadow length for {@code right} position.
     */
    private int getShadowLengthRight(int right) {
        int shadowLengthAmbient = getShadowLengthAmbient();
        if (right > mScreenWidth / 2f) {
            return Math.max(shadowLengthAmbient, (int) (mElevation / (mLightHeight / (mLightOffsetX + right))));
        } else {
            return shadowLengthAmbient;
        }
    }

    /**
     * Returns the bottom shadow length for {@code bottom} position.
     */
    private int getShadowLengthBottom(int bottom) {
        return Math.max(getShadowLengthAmbient(), (int) (mElevation / (mLightHeight / (mLightOffsetY + bottom))));
    }

    /**
     * Store the shadow calculation result ({@link Shader}s for each side, {@link Bitmap} for the corners) and
     * invalidate so that they are drawn on the next cycle.
     */
    @Override
    public void onShadowUpdate(int shadowLeftLength, int shadowTopLength, int shadowRightLength, int shadowBottomLength,
                               boolean leftDirty, boolean topDirty, boolean rightDirty, boolean bottomDirty,
                               Shader leftEdgeShader, Shader topEdgeShader,
                               Shader rightEdgeShader, Shader bottomEdgeShader,
                               Bitmap topLeftCornerBitmap, Bitmap topRightCornerBitmap,
                               Bitmap bottomRightCornerBitmap, Bitmap bottomLeftCornerBitmap) {
        mShadowLengthLeft = shadowLeftLength;
        mShadowLengthTop = shadowTopLength;
        mShadowLengthRight = shadowRightLength;
        mShadowLengthBottom = shadowBottomLength;

        if (leftDirty) {
            mShadowPaintLeft.setShader(leftEdgeShader);
        }
        if (topDirty) {
            mShadowPaintTop.setShader(topEdgeShader);
        }
        if (rightDirty || leftDirty) {
            mShadowPaintRight.setShader(rightEdgeShader);
        }
        if (bottomDirty || topDirty) {
            mShadowPaintBottom.setShader(bottomEdgeShader);
        }

        if (leftDirty || topDirty) {
            mShadowBitmapTopLeft = topLeftCornerBitmap;
        }
        if (topDirty || rightDirty) {
            mShadowBitmapTopRight = topRightCornerBitmap;
        }
        if (rightDirty || bottomDirty) {
            mShadowBitmapBottomRight = bottomRightCornerBitmap;
        }
        if (bottomDirty || leftDirty) {
            mShadowBitmapBottomLeft = bottomLeftCornerBitmap;
        }

        mIsShadowSetup = true;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            mInvalidateRunnable.run();
        } else {
            mHandler.post(mInvalidateRunnable);
        }
    }

    private float dpToPx(float dp) {
        View view = mViewRef.get();
        if (view != null) {
            DisplayMetrics metrics = view.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        } else {
            return 0;
        }
    }
}

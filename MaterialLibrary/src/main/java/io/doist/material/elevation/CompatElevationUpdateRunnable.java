package io.doist.material.elevation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.lang.ref.WeakReference;

/**
 * Creates {@link Shader} for drawing the edges of the shadow and {@link Bitmap} for drawing the corners.
 *
 * A {@link ShadowUpdateListener} is needed to obtain the result data.
 */
class CompatElevationUpdateRunnable implements Runnable {
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    private float mCornerRadius;

    private int mShadowLengthLeft;
    private int mShadowLengthTop;
    private int mShadowLengthRight;
    private int mShadowLengthBottom;

    private float mShadowAlphaLeft;
    private float mShadowAlphaTop;
    private float mShadowAlphaRight;
    private float mShadowAlphaBottom;

    private boolean mDirtyLeft;
    private boolean mDirtyTop;
    private boolean mDirtyRight;
    private boolean mDirtyBottom;

    private WeakReference<ShadowUpdateListener> mListenerRef;

    // Temporary variables reused while drawing each slice of each corner.
    private Path mTmpCornerSlicePath;
    private Paint mTmpCornerSlicePaint;
    private RectF mTmpCornerSliceRectF;
    private int[] mTmpCornerColors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT, -1, Color.TRANSPARENT};
    private float[] mTmpCornerStops = new float[]{0f, -1, -1, 1f};

    public CompatElevationUpdateRunnable(int left, int top, int right, int bottom, float cornerRadius,
                                         int shadowLengthLeft, int shadowLengthTop,
                                         int shadowLengthRight, int shadowLengthBottom,
                                         float shadowAlphaLeft, float shadowAlphaTop,
                                         float shadowAlphaRight, float shadowAlphaBottom,
                                         boolean dirtyLeft, boolean dirtyTop, boolean dirtyRight, boolean dirtyBottom,
                                         ShadowUpdateListener listener) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
        mCornerRadius = cornerRadius;
        mShadowLengthLeft = shadowLengthLeft;
        mShadowLengthTop = shadowLengthTop;
        mShadowLengthRight = shadowLengthRight;
        mShadowLengthBottom = shadowLengthBottom;
        mShadowAlphaLeft = shadowAlphaLeft;
        mShadowAlphaTop = shadowAlphaTop;
        mShadowAlphaRight = shadowAlphaRight;
        mShadowAlphaBottom = shadowAlphaBottom;
        mDirtyLeft = dirtyLeft;
        mDirtyTop = dirtyTop;
        mDirtyRight = dirtyRight;
        mDirtyBottom = dirtyBottom;
        mListenerRef = new WeakReference<>(listener);

        mTmpCornerSlicePath = new Path();
        mTmpCornerSlicePath.setFillType(Path.FillType.EVEN_ODD);
        mTmpCornerSlicePaint = new Paint();
        mTmpCornerSlicePaint.setStyle(Paint.Style.FILL);
        mTmpCornerSlicePaint.setColor(Color.BLACK);
        mTmpCornerSliceRectF = new RectF();
    }

    /**
     * Updates edge paints and corner bitmaps for the current state and given the dirty flags, and calls back the
     * {@link ShadowUpdateListener} when done.
     */
    @Override
    public void run() {
        int width = mRight - mLeft;
        int height = mBottom - mTop;

        Shader edgeShaderLeft = null;
        Shader edgeShaderTop = null;
        Shader edgeShaderRight = null;
        Shader edgeShaderBottom = null;
        Bitmap cornerBitmapTopLeft = null;
        Bitmap cornerBitmapTopRight = null;
        Bitmap cornerBitmapBottomRight = null;
        Bitmap cornerBitmapBottomLeft = null;

        // Build edge gradients and set them in the edge paints.
        if (mDirtyLeft) {
            edgeShaderLeft = buildEdgeShader(mShadowLengthLeft, 0, 0, 0, mShadowAlphaLeft);
        }
        if (mDirtyTop) {
            edgeShaderTop = buildEdgeShader(0, mShadowLengthTop, 0, 0, mShadowAlphaTop);
        }
        if (mDirtyRight || mDirtyLeft) {
            edgeShaderRight = buildEdgeShader(
                    (float) mShadowLengthLeft + width, 0,
                    (float) mShadowLengthLeft + width + mShadowLengthRight, 0, mShadowAlphaRight);
        }
        if (mDirtyBottom || mDirtyTop) {
            edgeShaderBottom = buildEdgeShader(
                    0, (float) mShadowLengthTop + height,
                    0, (float) mShadowLengthTop + height + mShadowLengthBottom, mShadowAlphaBottom);
        }

        // Build corner gradients (1 per slice) and draw them. Each corner bitmap is an alpha mask.
        // Drawing all paths with their respective paints directly is very expensive as there can be a lot of paths.
        Canvas canvas = new Canvas();
        if (mDirtyLeft || mDirtyTop) {
            cornerBitmapTopLeft = Bitmap.createBitmap(Math.round(mShadowLengthLeft + mCornerRadius),
                                                      Math.round(mShadowLengthTop + mCornerRadius),
                                                      Bitmap.Config.ALPHA_8);
            canvas.setBitmap(cornerBitmapTopLeft);
            drawCorner(canvas, mCornerRadius + mShadowLengthLeft, mCornerRadius + mShadowLengthTop,
                       mShadowLengthLeft, mShadowLengthTop, mShadowAlphaLeft, mShadowAlphaTop, 180f);
        }
        if (mDirtyTop || mDirtyRight) {
            cornerBitmapTopRight = Bitmap.createBitmap(Math.round(mShadowLengthRight + mCornerRadius),
                                                       Math.round(mShadowLengthTop + mCornerRadius),
                                                       Bitmap.Config.ALPHA_8);
            canvas.setBitmap(cornerBitmapTopRight);
            drawCorner(canvas, 0,  mShadowLengthTop + mCornerRadius,
                       mShadowLengthTop, mShadowLengthRight, mShadowAlphaTop, mShadowAlphaRight, -90f);
        }
        if (mDirtyRight || mDirtyBottom) {
            cornerBitmapBottomRight = Bitmap.createBitmap(Math.round(mShadowLengthRight + mCornerRadius),
                                                          Math.round(mShadowLengthBottom + mCornerRadius),
                                                          Bitmap.Config.ALPHA_8);
            canvas.setBitmap(cornerBitmapBottomRight);
            drawCorner(canvas, 0, 0,
                       mShadowLengthRight, mShadowLengthBottom, mShadowAlphaRight, mShadowAlphaBottom, 0f);
        }
        if (mDirtyBottom || mDirtyLeft) {
            cornerBitmapBottomLeft = Bitmap.createBitmap(Math.round(mShadowLengthLeft + mCornerRadius),
                                                         Math.round(mShadowLengthBottom + mCornerRadius),
                                                         Bitmap.Config.ALPHA_8);
            canvas.setBitmap(cornerBitmapBottomLeft);
            drawCorner(canvas, mCornerRadius + mShadowLengthLeft, 0,
                       mShadowLengthBottom, mShadowLengthLeft, mShadowAlphaBottom, mShadowAlphaLeft, 90f);
        }

        // Propagate the update to the callback.
        ShadowUpdateListener listener = mListenerRef.get();
        if (listener != null) {
            listener.onShadowUpdate(mShadowLengthLeft, mShadowLengthTop, mShadowLengthRight, mShadowLengthBottom,
                                    mDirtyLeft, mDirtyTop, mDirtyRight, mDirtyBottom,
                                    edgeShaderLeft, edgeShaderTop, edgeShaderRight, edgeShaderBottom,
                                    cornerBitmapTopLeft, cornerBitmapTopRight,
                                    cornerBitmapBottomRight, cornerBitmapBottomLeft);
        }
    }

    /**
     * Build a {@link LinearGradient} based on the passed-in coordinates and alpha.
     * Used for building edge gradients.
     */
    private Shader buildEdgeShader(float startX, float startY, float endX, float endY, float shadowAlpha) {
        return new LinearGradient(startX, startY, endX, endY,
                                  Color.argb((int) (255 * shadowAlpha), 0, 0, 0), Color.TRANSPARENT,
                                  Shader.TileMode.CLAMP);
    }

    /**
     * Draw a corner in the given {@link Canvas} using multiple slices to accommodate for different start / end
     * sizes. Each slice has its own {@link RadialGradient} to cross-fade between the start / end colors.
     * Used for building corner alpha masks.
     */
    private void drawCorner(Canvas canvas, float centerX, float centerY,
                            float startShadowLength, float endShadowLength,
                            float startShadowAlpha, float endShadowAlpha,
                            float startAngle) {
        int shadowDiff = (int) (endShadowLength - startShadowLength);
        int steps = Math.abs(shadowDiff) + 1;

        float sweepAngle = 90f / steps;
        float sweepShadowAlpha = (endShadowAlpha - startShadowAlpha) / (steps + 1);
        for (int i = 0; i < steps; i++) {
            buildCornerSlicePathPaint(
                    centerX, centerY,
                    startShadowLength + (shadowDiff > 0 ? i : -i), mCornerRadius,
                    startAngle + sweepAngle * i, sweepAngle,
                    startShadowAlpha + (i + 1) * sweepShadowAlpha);

            canvas.drawPath(mTmpCornerSlicePath, mTmpCornerSlicePaint);
        }
    }

    /**
     * Build both the {@link Path} and {@link Paint} for a given slice of a corner.
     */
    private void buildCornerSlicePathPaint(float centerX, float centerY,
                                           float shadowLength, float cornerRadius,
                                           float startingAngle, float sweepAngle,
                                           float shadowAlpha) {
        mTmpCornerSlicePath.rewind();
        float totalLength = shadowLength + cornerRadius;
        mTmpCornerSliceRectF.set(-totalLength, -totalLength, totalLength, totalLength);
        mTmpCornerSliceRectF.offset(centerX, centerY);
        mTmpCornerSlicePath.arcTo(mTmpCornerSliceRectF, startingAngle, sweepAngle);
        mTmpCornerSliceRectF.inset(totalLength, totalLength);
        mTmpCornerSlicePath.lineTo(centerX, centerY);
        mTmpCornerSlicePath.arcTo(mTmpCornerSliceRectF, startingAngle + sweepAngle, -sweepAngle);
        mTmpCornerSlicePath.close();

        mTmpCornerColors[2] = Color.argb((int) (255 * shadowAlpha), 0, 0, 0);
        mTmpCornerStops[2] = cornerRadius / totalLength;
        mTmpCornerStops[1] = Math.max(0f, mTmpCornerStops[2] - 1f / totalLength /* Poor man's AA. */);

        mTmpCornerSlicePaint.setShader(new RadialGradient(centerX, centerY, totalLength,
                                                          mTmpCornerColors, mTmpCornerStops,
                                                          Shader.TileMode.CLAMP));
    }

    /**
     * Listen for shadow updates following {@link CompatElevationUpdateRunnable} runs.
     */
    public interface ShadowUpdateListener {
        void onShadowUpdate(int shadowLeftLength, int shadowTopLength, int shadowRightLength, int shadowBottomLength,
                            boolean leftDirty, boolean topDirty, boolean rightDirty, boolean bottomDirty,
                            Shader leftEdgeShader, Shader topEdgeShader,
                            Shader rightEdgeShader, Shader bottomEdgeShader,
                            Bitmap topLeftCornerBitmap, Bitmap topRightCornerBitmap,
                            Bitmap bottomRightCornerBitmap, Bitmap bottomLeftCornerBitmap);
    }
}

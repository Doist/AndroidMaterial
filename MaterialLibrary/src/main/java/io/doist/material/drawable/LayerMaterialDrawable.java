package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class LayerMaterialDrawable extends LayerDrawable {
    /**
     * Padding mode used to nest each layer inside the padding of the previous
     * layer.
     *
     * @see #setPaddingMode(int)
     */
    public static final int PADDING_MODE_NEST = 0;

    /**
     * Padding mode used to stack each layer directly atop the previous layer.
     *
     * @see #setPaddingMode(int)
     */
    public static final int PADDING_MODE_STACK = 1;

    private static Class<?> LayerStateClass;

    protected WeakReference<Context> mContext;

    protected LayerMaterialState mLayerMaterialState;

    private int[] mPaddingL;
    private int[] mPaddingT;
    private int[] mPaddingR;
    private int[] mPaddingB;
    private final Rect mTempRect = new Rect();

    protected boolean mMutated;

    public LayerMaterialDrawable(Context context) {
        this(context, new Drawable[0]);
    }

    public LayerMaterialDrawable(Context context, Drawable[] layers) {
        super(layers);
        init(context, null);
    }

    LayerMaterialDrawable(LayerMaterialState state, Resources res) {
        super(new Drawable[0]);
        Object as = createLayerState(state.mLayerState, res);
        setLayerState(as);
        init(null, state);
    }

    private void init(Context context, LayerMaterialState state) {
        mContext = new WeakReference<>(context);
        mLayerMaterialState = createConstantState(state);
        mLayerMaterialState.mLayerState = super.getConstantState();

        if (getNumberOfLayers() > 0) {
            ensurePadding();
        }
    }

    Object createLayerState(Object state, Resources res) {
        if (LayerStateClass == null) {
            LayerStateClass =
                    ReflectionUtils.getClass(LayerDrawable.class.getName() + "$LayerState");
        }
        return ReflectionUtils.invokeDeclaredMethod(
                LayerDrawable.class,
                "createConstantState",
                new Class<?>[] {LayerStateClass, Resources.class},
                this,
                new Object[] {state, res});
    }

    void setLayerState(Object state) {
        ReflectionUtils.setDeclaredFieldValue(
                LayerDrawable.class,
                "mLayerState",
                this,
                state);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs, R.styleable.LayerDrawable);

        inflateWithAttributes(r, parser, a, R.styleable.LayerDrawable_android_visible);

        super.setOpacity(a.getInt(R.styleable.LayerDrawable_android_opacity, PixelFormat.UNKNOWN));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setAutoMirrored(a.getBoolean(R.styleable.LayerDrawable_android_autoMirrored, false));
        }

        setPaddingMode(a.getInteger(R.styleable.LayerDrawable_android_paddingMode, mLayerMaterialState.mPaddingMode));

        a.recycle();

        int type;
        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            a = r.obtainAttributes(attrs, R.styleable.LayerDrawableItem);

            int left = a.getDimensionPixelOffset(R.styleable.LayerDrawableItem_android_left, 0);
            int top = a.getDimensionPixelOffset(R.styleable.LayerDrawableItem_android_top, 0);
            int right = a.getDimensionPixelOffset(R.styleable.LayerDrawableItem_android_right, 0);
            int bottom = a.getDimensionPixelOffset(R.styleable.LayerDrawableItem_android_bottom, 0);
            int drawableRes = a.getResourceId(R.styleable.LayerDrawableItem_android_drawable, 0);
            int id = a.getResourceId(R.styleable.LayerDrawableItem_android_id, View.NO_ID);

            a.recycle();

            Context c = mContext.get();
            Drawable dr;
            if (drawableRes != 0) {
                dr = MaterialResources.getInstance(c, r).getDrawable(drawableRes);
            } else {
                while ((type = parser.next()) == XmlPullParser.TEXT) {
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(parser.getPositionDescription()
                                                             + ": <item> tag requires a 'drawable' attribute or "
                                                             + "child tag defining a drawable");
                }
                dr = MaterialDrawableUtils.createFromXmlInner(c, r, parser, attrs);
            }

            addLayer(dr, id, left, top, right, bottom);
        }

        ensurePadding();
        onStateChange(getState());
    }

    private void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int visibleAttr) {
        ReflectionUtils.invokeDeclaredMethod(
                Drawable.class,
                "inflateWithAttributes",
                new Class<?>[]{Resources.class, XmlPullParser.class, TypedArray.class, int.class},
                this,
                new Object[]{r, parser, attrs, visibleAttr});
    }

    protected void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        ReflectionUtils.invokeDeclaredMethod(
                LayerDrawable.class,
                "addLayer",
                new Class<?>[]{Drawable.class, int.class, int.class, int.class, int.class, int.class},
                this,
                new Object[]{layer, id, left, top, right, bottom});

        int index = getNumberOfLayers() - 1;
        mLayerMaterialState.setLayerInset(index, left, top, right, bottom);
    }

    public void setPaddingMode(int mode) {
        if (mLayerMaterialState.inCompat) {
            mLayerMaterialState.setPaddingMode(mode);
        } else {
            super.setPaddingMode(mode);
        }
    }

    public int getPaddingMode() {
        if (mLayerMaterialState.inCompat) {
            return mLayerMaterialState.mPaddingMode;
        } else {
            return super.getPaddingMode();
        }
    }

    @Override
    public boolean getPadding(Rect padding) {
        if (mLayerMaterialState.handlePaddingModeStack) {
            return getPaddingCompat(padding);
        } else {
            return super.getPadding(padding);
        }
    }

    protected boolean getPaddingCompat(Rect padding) {
        padding.left = 0;
        padding.top = 0;
        padding.right = 0;
        padding.bottom = 0;

        final int N = getNumberOfLayers();
        for (int i = 0; i < N; i++) {
            refreshChildPadding(i, getDrawable(i));

            // Take the max padding.
            padding.left = Math.max(padding.left, mPaddingL[i]);
            padding.top = Math.max(padding.top, mPaddingT[i]);
            padding.right = Math.max(padding.right, mPaddingR[i]);
            padding.bottom = Math.max(padding.bottom, mPaddingB[i]);
        }

        return padding.left != 0 || padding.top != 0 || padding.right != 0 || padding.bottom != 0;
    }

    /**
     * Refreshes the cached padding values for the specified child.
     */
    private void refreshChildPadding(int i, Drawable drawable) {
        final Rect rect = mTempRect;
        drawable.getPadding(rect);
        if (rect.left != mPaddingL[i] || rect.top != mPaddingT[i] ||
                rect.right != mPaddingR[i] || rect.bottom != mPaddingB[i]) {
            mPaddingL[i] = rect.left;
            mPaddingT[i] = rect.top;
            mPaddingR[i] = rect.right;
            mPaddingB[i] = rect.bottom;
        }
    }

    public void setLayerInset(int index, int l, int t, int r, int b) {
        super.setLayerInset(index, l, t, r, b);
        mLayerMaterialState.setLayerInset(index, l, t, r, b);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (mLayerMaterialState.handlePaddingModeStack) {
            onBoundsChangeCompat(bounds);
        } else {
            super.onBoundsChange(bounds);
        }
    }

    protected void onBoundsChangeCompat(Rect bounds) {
        final int N = getNumberOfLayers();
        for (int i = 0; i < N; i++) {
            Drawable layerDrawable = getDrawable(i);
            // When onBoundsChange is called, it means that some children's padding changed.
            // Refresh child padding.
            refreshChildPadding(i, layerDrawable);

            // Update child bounds.
            int[] inset = mLayerMaterialState.getLayerInset(i);
            layerDrawable.setBounds(
                    bounds.left + inset[0],
                    bounds.top + inset[1],
                    bounds.right - inset[2],
                    bounds.bottom - inset[3]);
        }
    }

    @Override
     public int getIntrinsicWidth() {
        if (mLayerMaterialState.handlePaddingModeStack) {
            return getIntrinsicWidthCompat();
        } else {
            return super.getIntrinsicWidth();
        }
    }

    public int getIntrinsicWidthCompat() {
        int width = -1;

        final int N = getNumberOfLayers();
        for (int i = 0; i < N; i++) {
            Drawable layerDrawable = getDrawable(i);
            int[] inset = mLayerMaterialState.getLayerInset(i);
            final int w = layerDrawable.getIntrinsicWidth() + inset[0] + inset[2];
            if (w > width) {
                width = w;
            }
        }

        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        if (mLayerMaterialState.handlePaddingModeStack) {
            return getIntrinsicHeightCompat();
        } else {
            return super.getIntrinsicHeight();
        }
    }

    public int getIntrinsicHeightCompat() {
        int height = -1;

        final int N = getNumberOfLayers();
        for (int i = 0; i < N; i++) {
            Drawable layerDrawable = getDrawable(i);
            int[] inset = mLayerMaterialState.getLayerInset(i);
            int h = layerDrawable.getIntrinsicHeight() + inset[1] + inset[3];
            if (h > height) {
                height = h;
            }
        }

        return height;
    }

    private void ensurePadding() {
        ReflectionUtils.invokeDeclaredMethod(
                LayerDrawable.class,
                "ensurePadding",
                ReflectionUtils.EMPTY_TYPES,
                this,
                ReflectionUtils.EMPTY_PARAMETERS);

        final int N = getNumberOfLayers();
        if (mPaddingL != null && mPaddingL.length >= N) {
            return;
        }
        mPaddingL = new int[N];
        mPaddingT = new int[N];
        mPaddingR = new int[N];
        mPaddingB = new int[N];
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mLayerMaterialState = createConstantState(mLayerMaterialState);
            mLayerMaterialState.mLayerState = super.getConstantState();
            // Make a deep close of the layer insets sparse array.
            int N = mLayerMaterialState.mLayerInsets.size();
            SparseArray<int[]> layerInsets = new SparseArray<>(N);
            for (int i = 0; i < N; i++) {
                int[] layerInset = new int[4];
                System.arraycopy(mLayerMaterialState.mLayerInsets.valueAt(i), 0, layerInset, 0, 4);
                layerInsets.put(mLayerMaterialState.mLayerInsets.keyAt(i), layerInset);
            }
            mLayerMaterialState.mLayerInsets = layerInsets;
            mMutated = true;
        }
        return this;
    }

    @Override
    public ConstantState getConstantState() {
        mLayerMaterialState.mLayerState = super.getConstantState();
        return mLayerMaterialState;
    }

    LayerMaterialState createConstantState(LayerMaterialState state) {
        return new LayerMaterialState(state);
    }

    static class LayerMaterialState extends ConstantState {
        boolean inCompat;

        ConstantState mLayerState;
        int mPaddingMode;
        private SparseArray<int[]> mLayerInsets;
        // True if this class handle all things related to PADDING_MODE_STACK.
        // This includes: padding, bounds, intrinsic width and intrinsic height.
        boolean handlePaddingModeStack;

        LayerMaterialState(LayerMaterialState state) {
            inCompat = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

            if (state != null) {
                mLayerState = state.mLayerState;
                setPaddingMode(state.mPaddingMode);
                mLayerInsets = state.mLayerInsets;
            } else {
                // Default padding mode.
                setPaddingMode(PADDING_MODE_NEST);
                mLayerInsets = new SparseArray<>();
            }
        }

        public void setPaddingMode(int mode) {
            mPaddingMode = mode;
            handlePaddingModeStack = inCompat && mPaddingMode == PADDING_MODE_STACK;
        }

        public void setLayerInset(int index, int l, int t, int r, int b) {
            int[] layerInset = getLayerInset(index);
            layerInset[0] = l;
            layerInset[1] = t;
            layerInset[2] = r;
            layerInset[3] = b;
        }

        public int[] getLayerInset(int index) {
            int[] layerInset = mLayerInsets.get(index);
            if (layerInset == null) {
                layerInset = new int[4];
                mLayerInsets.put(index, layerInset);
            }
            return layerInset;
        }

        @Override
        public Drawable newDrawable() {
            return new LayerMaterialDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new LayerMaterialDrawable(this, res);
        }

        @Override
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return new LayerMaterialDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mLayerState != null ? mLayerState.getChangingConfigurations() : 0;
        }
    }
}

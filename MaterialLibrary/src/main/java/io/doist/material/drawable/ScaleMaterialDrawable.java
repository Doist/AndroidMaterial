package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class ScaleMaterialDrawable extends ScaleDrawable {
    private final WeakReference<Context> mContext;

    public ScaleMaterialDrawable(Context context) {
        super(null, Gravity.LEFT, -1.0f, -1.0f);
        mContext = new WeakReference<>(context);
    }

    private static float getPercent(TypedArray a, int name) {
        String s = a.getString(name);
        if (s != null) {
            if (s.endsWith("%")) {
                String f = s.substring(0, s.length() - 1);
                return Float.parseFloat(f) / 100.0f;
            }
        }
        return -1;
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        int type;

        TypedArray a = r.obtainAttributes(attrs, R.styleable.ScaleDrawable);

        float sw = getPercent(a, R.styleable.ScaleDrawable_android_scaleWidth);
        float sh = getPercent(a, R.styleable.ScaleDrawable_android_scaleHeight);
        int g = a.getInt(R.styleable.ScaleDrawable_android_scaleGravity, Gravity.LEFT);
        boolean min = a.getBoolean(R.styleable.ScaleDrawable_android_useIntrinsicSizeAsMinimum, false);
        int drawableRes = a.getResourceId(R.styleable.ScaleDrawable_android_drawable, 0);
        Context c = mContext.get();
        Drawable dr = null;
        if (drawableRes != 0) {
            dr = MaterialResources.getInstance(c, r).getDrawable(drawableRes);
        }

        a.recycle();

        final int outerDepth = parser.getDepth();
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            dr = MaterialDrawableUtils.createFromXmlInner(c, r, parser, attrs);
        }

        if (dr == null) {
            throw new IllegalArgumentException("No drawable specified for <scale>");
        }

        ScaleState mScaleState = new ScaleState(this);
        mScaleState.setDrawable(dr);
        mScaleState.setScaleWidth(sw);
        mScaleState.setScaleHeight(sh);
        mScaleState.setGravity(g);
        mScaleState.setUseIntrinsicSizeAsMin(min);

        if (dr != null) {
            dr.setCallback(this);
        }
    }

    /**
     * Helper class to manipulate internal member mScaleState.
     */
    private static class ScaleState {
        final Class<?> ScaleStateClass;
        final Object mScaleState;

        public ScaleState(ScaleMaterialDrawable receiver) {
            ScaleStateClass = ReflectionUtils.getClass(ScaleDrawable.class.getName() + "$ScaleState");

            mScaleState = ReflectionUtils.getDeclaredFieldValue(
                    ScaleDrawable.class,
                    "mScaleState",
                    receiver);
        }

        public void setDrawable(Drawable drawable) {
            setField("mDrawable", drawable);
        }

        public void setScaleWidth(float scaleWidth) {
            setField("mScaleWidth", scaleWidth);
        }

        public void setScaleHeight(float scaleHeight) {
            setField("mScaleHeight", scaleHeight);
        }

        public void setGravity(int gravity) {
            setField("mGravity", gravity);
        }

        public void setUseIntrinsicSizeAsMin(boolean useIntrinsicSizeAsMin) {
            setField("mUseIntrinsicSizeAsMin", useIntrinsicSizeAsMin);
        }

        private void setField(String fieldName, Object value) {
            ReflectionUtils.setDeclaredFieldValue(ScaleStateClass, fieldName, mScaleState, value);
        }
    }
}

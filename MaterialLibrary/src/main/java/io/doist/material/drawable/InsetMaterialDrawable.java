package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class InsetMaterialDrawable extends InsetDrawable {
    private final WeakReference<Context> mContext;

    public InsetMaterialDrawable(Context context) {
        super(null, 0);
        mContext = new WeakReference<>(context);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        int type;

        TypedArray a = r.obtainAttributes(attrs, R.styleable.InsetDrawable);

        inflateWithAttributes(r, parser, a, R.styleable.InsetDrawable_android_visible);

        int drawableRes = a.getResourceId(R.styleable.InsetDrawable_android_drawable, 0);

        int inLeft = a.getDimensionPixelOffset(R.styleable.InsetDrawable_android_insetLeft, 0);
        int inTop = a.getDimensionPixelOffset(R.styleable.InsetDrawable_android_insetTop, 0);
        int inRight = a.getDimensionPixelOffset(R.styleable.InsetDrawable_android_insetRight, 0);
        int inBottom = a.getDimensionPixelOffset(R.styleable.InsetDrawable_android_insetBottom, 0);

        a.recycle();

        Context c = mContext.get();
        Drawable dr;
        if (drawableRes != 0) {
            dr = MaterialResources.getInstance(c, r).getDrawable(drawableRes);
        } else {
            while ((type = parser.next()) == XmlPullParser.TEXT) {
            }
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException(
                        parser.getPositionDescription()
                                + ": <inset> tag requires a 'drawable' attribute or "
                                + "child tag defining a drawable");
            }
            dr = MaterialDrawableUtils.createFromXmlInner(c, r, parser, attrs);
        }

        if (dr == null) {
            Log.w("drawable", "No drawable specified for <inset>");
        }

        InsetState mInsetState = new InsetState(this);
        mInsetState.setDrawable(dr);
        mInsetState.setInsetLeft(inLeft);
        mInsetState.setInsetRight(inRight);
        mInsetState.setInsetTop(inTop);
        mInsetState.setInsetBottom(inBottom);

        if (dr != null) {
            dr.setCallback(this);
        }
    }

    private void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int visibleAttr) {
        ReflectionUtils.invokeDeclaredMethod(
                Drawable.class,
                "inflateWithAttributes",
                new Class<?>[]{Resources.class, XmlPullParser.class, TypedArray.class, int.class},
                this,
                new Object[]{r, parser, attrs, visibleAttr});
    }

    /**
     * Helper class to manipulate internal member mInsetState.
     */
    private static class InsetState {
        final Class<?> InsetStateClass;
        final Object mInsetState;

        public InsetState(InsetMaterialDrawable receiver) {
            InsetStateClass = ReflectionUtils.getClass(InsetDrawable.class.getName() + "$InsetState");

            mInsetState = ReflectionUtils.getDeclaredFieldValue(
                    InsetDrawable.class,
                    "mInsetState",
                    receiver);
        }

        public void setDrawable(Drawable drawable) {
            setField("mDrawable", drawable);
        }

        public void setInsetLeft(int inLeft) {
            setField("mInsetLeft", inLeft);
        }

        public void setInsetRight(int inRight) {
            setField("mInsetRight", inRight);
        }

        public void setInsetTop(int inTop) {
            setField("mInsetTop", inTop);
        }

        public void setInsetBottom(int inBottom) {
            setField("mInsetBottom", inBottom);
        }

        private void setField(String fieldName, Object value) {
            ReflectionUtils.setDeclaredFieldValue(InsetStateClass, fieldName, mInsetState, value);
        }
    }
}

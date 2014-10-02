package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.doist.material.R;
import io.doist.material.res.MaterialResources;

public class LayerMaterialDrawable extends LayerDrawable {
    private static final String TAG = LayerMaterialDrawable.class.getSimpleName();

    private WeakReference<Context> mContext;

    public LayerMaterialDrawable(Context context) {
        super(new Drawable[0]);
        mContext = new WeakReference<>(context);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs, R.styleable.LayerDrawable);

        inflateWithAttributes(r, parser, a, R.styleable.LayerDrawable_android_visible);

        super.setOpacity(a.getInt(R.styleable.LayerDrawable_android_opacity, PixelFormat.UNKNOWN));

        setAutoMirrored(a.getBoolean(R.styleable.LayerDrawable_android_autoMirrored, false));

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
        try {
            final Method inflateWithAttributes = Drawable.class.getDeclaredMethod(
                    "inflateWithAttributes",
                    Resources.class,
                    XmlPullParser.class,
                    TypedArray.class,
                    int.class);

            inflateWithAttributes.setAccessible(true);
            inflateWithAttributes.invoke(
                    this,
                    r,
                    parser,
                    attrs,
                    visibleAttr);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.w(TAG, e);
        }
    }

    private void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        try {
            final Method addLayer = LayerDrawable.class.getDeclaredMethod(
                    "addLayer",
                    Drawable.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class);
            addLayer.setAccessible(true);
            addLayer.invoke(this, layer, id, left, top, right, bottom);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            Log.w(TAG, e);
        }
    }

    private void ensurePadding() {
        try {
            final Method ensurePadding = LayerDrawable.class.getDeclaredMethod("ensurePadding");
            ensurePadding.setAccessible(true);
            ensurePadding.invoke(this);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            Log.w(TAG, e);
        }
    }
}

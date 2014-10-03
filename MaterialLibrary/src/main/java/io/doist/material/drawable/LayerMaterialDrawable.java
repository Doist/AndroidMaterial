package io.doist.material.drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.doist.material.R;
import io.doist.material.reflection.ReflectionUtils;
import io.doist.material.res.MaterialResources;

public class LayerMaterialDrawable extends LayerDrawable {
    final private WeakReference<Context> mContext;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setAutoMirrored(a.getBoolean(R.styleable.LayerDrawable_android_autoMirrored, false));
        }

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

    private void addLayer(Drawable layer, int id, int left, int top, int right, int bottom) {
        ReflectionUtils.invokeDeclaredMethod(
                LayerDrawable.class,
                "addLayer",
                new Class<?>[] {Drawable.class, int.class, int.class, int.class, int.class, int.class},
                this,
                new Object[] {layer, id, left, top, right, bottom});
    }

    private void ensurePadding() {
        ReflectionUtils.invokeDeclaredMethod(
                LayerDrawable.class,
                "ensurePadding",
                new Class<?>[0],
                this,
                new Object[0]);
    }
}
